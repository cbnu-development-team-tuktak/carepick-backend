package com.callrapport.component.crawler

// Component 및 서비스 관련 import 
import com.callrapport.component.crawler.WebCrawler // WebCrawler: 웹 크롤링을 위한 커스텀 클래스
import com.callrapport.component.extractor.DoctorInfoExtractor // DoctorInfoExtractor: 의사 정보를 HTML에서 추출하는 유틸리티 클래스
import com.callrapport.service.DoctorService // 크롤링한 데이터를 저장하는 서비스

// Jsoup (HTML 파싱 라이브러리) 관련 import
import org.jsoup.Jsoup // HTML 문서를 다운로드하고, DOM을 분석하는 라이브러리
import org.jsoup.nodes.Document // Jsoup에서 HTML 문서를 나타내는 객체
import org.jsoup.nodes.Element // Jsoup에서 특정 HTML 요소를 나타내는 객체

// Selenium (웹 자동화) 관련 import 
import org.openqa.selenium.By // HTML 요소를 찾기 위한 클래스 (CSS Selector, XPath 등 지원)
import org.openqa.selenium.support.ui.WebDriverWait // 웹 페이지 로딩을 기다리는 기능
import org.openqa.selenium.support.ui.ExpectedConditions // 특정 조건이 만족할 때까지 대기하는 기능
import java.time.Duration // Selenium의 대기 시간을 설정하기 위한 클래스

// Spring 관련 import 
import org.springframework.stereotype.Component // 해당 클래스를 Spring의 빈(Bean)으로 등록하는 어노테이션

@Component
class DoctorCrawler(
    private val webCrawler: WebCrawler, // 웹 브라우저 조작을 위한 크롤러
    private val doctorInfoExtractor: DoctorInfoExtractor, // HTML 문서에서 의사 정보를 추출하는 유틸리티
    private val doctorService: DoctorService // 크롤링한 데이터를 DB에 저장하는 서비스
) {
    // 의사 프로필 페이지 링크를 크롤링하는 메서드
    fun crawlDoctorLinks(): List<Pair<String, String>> {
        val doctorLinks = mutableListOf<Pair<String, String>>() // 의사 링크를 저장할 리스트
        var pageNum = 1 // 현재 페이지 번호

        while (true) {
            // 하이닥 의사·병원 찾기 페이지 URL
            val url = "https://www.hidoc.co.kr/find/result/list?orderType=15010&page=${pageNum}&filterType=D"
            val doc: Document = Jsoup.connect(url).get()

            // 의사 이름과 프로필 페이지 링크가 포함된 요소를 선택
            val doctorElements = doc.select("div.search_result_list ul.search_list li.item.item_2 strong.name a")
            
            // 더 이상 의사 목록이 없으면 크롤링 종료
            if (doctorElements.isEmpty()) break
            
            // 크롤링한 의사 정보를 리스트에 추가
            for (element: Element in doctorElements) {
                val name = element.text() // 의사 이름
                val link = "https://www.hidoc.co.kr" + element.attr("href") // 상세 페이지 링크
                doctorLinks.add(name to link) // 의사 이름, 링크
            }

            println("$pageNum page") // 현재 페이지 번호 출력
            println("new found doctor links count: ${doctorElements.size}") // 새롭게 발견된 링크 개수 출력
            println("total found doctor links count: ${doctorLinks.size}") // 전체 발견된 링크 개수 출력

            pageNum++ // 다음 페이지로 이동
        }
        return doctorLinks
    }

    fun crawlDoctorInfos(
        name: String, // 의사 이름
        url: String // 의사 프로필 페이지 URL
    ): Map<String, String?> {
        val driver = webCrawler.createWebDriver() // 웹 드라이버 생성
    
        try { 
            driver.get(url) // 해당 URL의 웹페이지 열기
            
            // 프로필 이미지가 로드될 때까지 대기 (최대 10초)
            WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.thumb_img img")))
    
            val doc: Document = Jsoup.parse(driver.pageSource) // 페이지 소스를 Jsoup 문서로 변환
    
            // 의사 id를 추출 (URL의 마지막 부분을 사용)
            val id = url.substringAfterLast("/", "").takeIf { it.isNotEmpty() } ?: ""
    
            // 크롤링한 데이터를 Map으로 정리
            val doctorData = mapOf(
                "id" to id,
                "name" to name,
                "profileImage" to doctorInfoExtractor.extractProfileImage(doc),
                "educationLicenses" to doctorInfoExtractor.extractEducationLicenses(doc)?.joinToString(", "), 
                "hospitalId" to doctorInfoExtractor.extractHospitalId(doc),
                "specialty" to doctorInfoExtractor.extractSpecialty(doc),
            )
            
            // 크롤링한 데이터를 데이터베이스에 저장
            doctorService.saveDoctorWithDetails(
                id = doctorData["id"]!!,
                name = doctorData["name"]!!,
                profileImage = doctorData["profileImage"],
                educationLicenses = doctorInfoExtractor.extractEducationLicenses(doc) ?: emptyList(), 
                hospitalId = doctorData["hospitalId"],
                specialtyName = doctorData["specialty"]
            )
    
            println("$name doctor data saved successfully") // 저장 완료 로그 출력
            return doctorData
    
        } catch (e: Exception) {
            return errorResponse(name, url, e.message ?: "Unknown error") // 오류 발생 시 errorResponse 반환
        } finally {
            driver.quit() // 웹 드라이버 종료
        }
    }
    
    // 오류 발생 시 기본 응답을 반환하는 메서드
    private fun errorResponse(
        name: String, // 의사 이름
        url: String, // 프로필 페이지 URL 
        message: String // 오류 메시지 
    ): Map<String, String?> {
        println("⚠️ Failed to crawl doctor info from $url: $message")
        return mapOf(
            "id" to "",
            "name" to name,
            "profileImage" to "",
            "educationLicenses" to null, 
            "hospitalId" to "",
            "specialty" to "",
            "error" to "⚠️ $message"
        )
    }
}
