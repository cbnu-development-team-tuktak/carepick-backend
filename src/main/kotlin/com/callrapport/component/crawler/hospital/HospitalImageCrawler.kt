package com.callrapport.component.crawler.hospital

// WebCrawler 관련 import
import com.callrapport.component.crawler.WebCrawler // Selenium을 이용한 웹 크롤링 기능 제공

// 이미지 모델 및 레포지토리 import
import com.callrapport.model.common.Image // 병원에 사용되는 이미지 모델
import com.callrapport.repository.common.ImageRepository // 이미지 정보를 DB에 저장 및 조회하는 JPA 레포지토리

// Selenium 관련 라이브러리 
import org.openqa.selenium.By // HTML 요소를 찾기 위한 선택자 크래스
import org.openqa.selenium.WebElement // HTML 요소를 나타내는 객체
import org.openqa.selenium.support.ui.ExpectedConditions // 특정 조건이 만족될 때까지 대기
import org.openqa.selenium.support.ui.WebDriverWait // 웹 요소가 로드될 때까지 대기하는 클래스

// Spring 관련 import 
import org.springframework.stereotype.Component // Component: 해당 클래스를 Spring Bean으로 등록

// URL 인코딩/디코딩 관련 import 
import java.net.URLEncoder // 문자열을 URL 형식에 맞게 인코딩
import java.net.URLDecoder // URL 형식으로 인코딩된 문자열을 디코딩
import java.nio.charset.StandardCharsets // UTF-8 등의 문자 인코딩 상수 제공

// 시간 관련 import
import java.time.Duration // WebDriverWait 등의 시간 설정 시 사용
import java.security.MessageDigest

@Component
class HospitalImageCrawler(
    private val webCrawler: WebCrawler, // Seleium WebDriver를 생성하는 유틸리티 클래스
    private val imageRepository: ImageRepository // 이미지 정보를 DB에서 조회 및 저장하는 레포지토리
) {
    fun String.sha256(): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // 썸네일 URL에서 원본 이미지를 추출
    fun extractOriginalImageUrl(thumbnailUrl: String): String {
        return try {
            // 썸네일 URL에서 `src=` 다음에 오는 인코딩된 원본 URL을 정규식으로 추출
            val regex = Regex("src=([^&]+)") 
            val match = regex.find(thumbnailUrl)
            if (match != null) {
                val encodedUrl = match.groupValues[1]
                // 추출된 URL을 디코딩하여 원본 이미지 반환
                URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString()) 
            } else {
              // 정규식에 매칭되지 않으면 원래의 썸네일 URL 반환
                thumbnailUrl 
            }
        } catch (e: Exception) {
            // 에러 발생 시 로그 출력하고 썸네일 URL 반환
            println("⚠️ Failed to extract original URL: ${e.message}")
            thumbnailUrl
        }
    }

    // 네이버 이미지 검색을 통해 병원 이미지를 크롤링
    fun crawlHospitalImages(hospitalName: String): List<Image> {
        val driver = webCrawler.createWebDriver() // WebDriver 인스턴스 생성
        return try {
            // 병원 이름을 URL 인코딩하여 검색 쿼리에 사용
            val encodedHospitalName = URLEncoder.encode(hospitalName, StandardCharsets.UTF_8.toString())
            val searchUrl = "https://search.naver.com/search.naver?where=image&mode=column&section=place&query=$encodedHospitalName"
    
            println("🔍 Opening Naver Image Search: $searchUrl")
            driver.get(searchUrl) // 네이버 이미지 검색 페이지 접속
            
            // 검색 결과 이미지 요소가 나타날 때까지 최대 20초 대기
            val wait = WebDriverWait(driver, Duration.ofSeconds(20))
    
            wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div.tile_item._fe_image_tab_content_tile[data-type='image']")
                )
            )
            
            // 이미지가 포함된 <div> 요소들을 모두 선택
            val imageDivs: List<WebElement> = driver.findElements(By.cssSelector("div.tile_item._fe_image_tab_content_tile[data-type='image']"))
            
            // 각 div 요소에서 썸네일 URL을 추출하고, 원본 URL로 변환한 후 이미지 리스트 생성
            val imageList = imageDivs.mapNotNull { div ->
                try {
                    // <img> 태그를 선택하여 썸네일 이미지 URL 가져오기
                    val imgElement = div.findElement(By.cssSelector("img._fe_image_tab_content_thumbnail_image"))
                    val thumbnailUrl = imgElement.getAttribute("src")
                    
                    // 유효한 URL이고 네이버 썸네일 도메인일 경우만 처리 
                    if (!thumbnailUrl.isNullOrBlank() && thumbnailUrl.startsWith("https://search.pstatic.net/common")) {
                        val originalUrl = extractOriginalImageUrl(thumbnailUrl) // 썸네일에서 원본 URL 추출
    
                        // DB에 해당 URL이 이미 존재하는지 확인
                        val existingImage = imageRepository.findByUrl(originalUrl)
                        
                        if (existingImage != null) { // 이미지가 이미 DB에 존재하는 경우
                            println("⚠️ Image already exists in DB: $originalUrl")
                            existingImage // 기존 이미지 반환
                        } else { // 이미지가 DB에 존재하지 않는 경우
                            println("✅ New image found, adding to DB: $originalUrl")
                            // 새로운 이미지 객체 생성 
                           Image(
                                url = originalUrl,
                                urlHash = originalUrl.sha256(),
                                alt = "${hospitalName}_이미지"
                            )
                        }
                    } else {
                        null // 유효하지 않은 URL은 필터링 
                    }
                } catch (e: Exception) {
                    null // 개별 요소 처리 실패 시 무시
                }
            }.filterNotNull().take(5) // 최대 5개까지만 결과 반환
    
            println("✅ Extracted ${imageList.size} high-resolution image URLs: $imageList")
            return imageList
        } catch (e: Exception) {
            // 전체 과정에서 오류 발생 시 로그 출력 후 빈 리스트 반환
            println("⚠️ Failed to extract images: ${e.message}")
            emptyList()
        } finally {
            driver.quit() // WebDriver 종료
        }
    }
}
