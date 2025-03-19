package com.callrapport.component.crawler.hospital

// Selenium 관련 라이브러리
import org.openqa.selenium.By // HTML 요소를 찾기 위한 선택자 클래스
import org.openqa.selenium.WebDriver // 웹 브라우저를 조작하는 인터페이스 
import org.openqa.selenium.WebElement // HTML 요소를 나타내는 객체
import org.openqa.selenium.chrome.ChromeDriver // Chrome 브라우저를 자동화하기 위한 WebDriver
import org.openqa.selenium.chrome.ChromeOptions // Chrome 실행 옵션을 설정하는 클래스
import org.openqa.selenium.support.ui.ExpectedConditions // 특정 조건이 만족할 때까지 대기하는 기능 제공
import org.openqa.selenium.support.ui.WebDriverWait // 웹 페이지가 로딩될 때까지 대기하는 기능 제공

// Spring 관련 라이브러리
import org.springframework.stereotype.Component // Spring의 컴포넌트로 등록하기 위한 어노테이션

// 기타 유틸리티
import java.time.Duration // Selenium의 대기 시간을 설정하기 위한 Duration 클래스

@Component
class HospitalImageCrawler(
    private val webCrawler: WebCrawler
) {
    fun testNavigation(hospitalName: String): List<String> {
        val driver = webCrawler.createWebDriver() // WebDriver 생성
        return try {
            // 최대 60초까지 대기 설정
            val wait = WebDriverWait(driver, Duration.ofSeconds(60))

            // 네이버 지도 검색 페이지 열기
            val searchUrl = "https://map.naver.com/p/search/$hospitalName"
            driver.get(searchUrl)
            println("🔍 Opened Naver Map search page: $searchUrl")

            // entryFrame이 로드될 때까지 대기 후 전환
            val entryIframe = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("iframe#entryIframe"))
            )
            driver.switchTo().frame(entryIframe)
            println("✅ Switched to entryIframe.")

            // entryIframe의 콘텐츠가 완전히 로드될 때까지 대기 (최대 20초)
            println("⏳ Waiting for entryIframe content to load (max 20s)...")
            Thread.sleep(20000) // 20초 대기

            // 현재 entryIframe의 URL 가져오기
            val entryUrl = driver.currentUrl
            println("🌍 Extracted entryIframe URL: $entryUrl")

            // 사진 탭이 나타날 때까지 대기
            println("⏳ Waiting for photo tab to appear...")
            var photoTab: WebElement? = null
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < 60000) { // 최대 60초 대기
                try {
                    // 사진 탭을 포함하는 a 태그 검색
                    photoTab = driver.findElement(
                        By.xpath("//a[contains(@class, 'tab-menu') and contains(., '사진')]")
                    )
                    if (photoTab != null) break // 탭 찾으면 바로 탈출
                } catch (_: Exception) {
                    Thread.sleep(1000) // 1초 대기 후 재시도
                }
            }

            if (photoTab == null) {
                println("❌ photo tab not found within 60 seconds.")
                return emptyList()
            }

            println("📸 Found '사진' tab. Clicking now...")
            photoTab!!.click() // 사진 탭 클릭

            // 이미지가 로드될 때까지 대기 (최대 60초 대기)
            println("⏳ Waiting for images to appear...")
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("img.K0PDV")))
            println("✅ Images are now visible!")

            // 이미지 URL 추출 (최대 5개)
            val images = driver.findElements(By.cssSelector("img.K0PDV")) // 'KOPDV' 클래스를 가진 이미지 요소 찾기
            val imageUrls = images.take(5).map { it.getAttribute("src") }.filterNotNull() // 최대 5개 이미지 URL 추출

            println("✅ Extracted image URLs: $imageUrls")

            return imageUrls // 병원 이미지 목록 반환
        } catch (e: Exception) {
            println("⚠️ Navigation test failed: ${e.message}")
            return emptyList() // 실패 시 빈 리스트 반환
        } finally {
            driver.quit() // 웹 드라이버 종료
        }
    }
}