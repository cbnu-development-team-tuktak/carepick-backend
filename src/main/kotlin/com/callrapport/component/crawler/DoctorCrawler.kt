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
    fun crawlDoctorInfos(
        id: String,  // 의사 ID
        name: String, // 의사 이름
        url: String   // 의사 프로필 페이지 URL
    ): Map<String, String?> {
        val driver = webCrawler.createWebDriver() // 웹 드라이버 생성

        try {
            // ✅ 매개변수로 받은 ID 확인
            println("🚀 Inside crawlDoctorInfos() - Received ID: $id, Name: $name, URL: $url")

            driver.get(url) // 해당 URL의 웹페이지 열기

            val doc: Document = Jsoup.parse(driver.pageSource) // 페이지 소스를 Jsoup 문서로 변환

            // ✅ 진료과 정보 추출
            val specialty = doctorInfoExtractor.extractSpecialty(doc)

            // 경력 추출
            val career = doctorInfoExtractor.extractCareer(doc)

            // 자격/면허 정보 추출
            val educationLicense = doctorInfoExtractor.extractEducationLicenses(doc)

            // ✅ 크롤링한 데이터를 Map으로 정리
            val doctorData = mapOf(
                "id" to id,  // ✅ ID 유지
                "name" to name,  // ✅ 이름 유지
                "url" to url,  // ✅ URL 유지
                "specialty" to specialty,  // ✅ 진료과 정보 추가
                "career" to career, // 경력 정보
                "educationLicense" to educationLicense
            )

            // ✅ 반환 직전 데이터 확인
            println("🔍 Doctor data before return: $doctorData")

            return doctorData

        } catch (e: Exception) {
            return errorResponse(id, name, url, e.message ?: "Unknown error") // 오류 발생 시 errorResponse 반환
        } finally {
            driver.quit() // 웹 드라이버 종료
        }
    }

    // 오류 발생 시 기본 응답을 반환하는 메서드
    private fun errorResponse(
        id: String, // 의사 ID
        name: String, // 의사 이름
        url: String, // 프로필 페이지 URL
        message: String // 오류 메시지
    ): Map<String, String?> {
        println("⚠️ Failed to crawl doctor info from $url: $message")

        return mapOf(
            "id" to id,  // ✅ 기존 id 유지
            "name" to name,  // ✅ 기존 name 유지
            "url" to url,  // ✅ 기존 url 유지
            "specialty" to "", // 진료과 정보 없음
            "error" to "⚠️ $message" // 오류 메시지 추가
        )
    }
}
