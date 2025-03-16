package com.callrapport.component.crawler

// WebCrawler 및 병원 정보 추출기 관련 import
import com.callrapport.component.crawler.WebCrawler // WebCrawler: Selenium을 이용한 웹 크롤링 기능 제공
import com.callrapport.component.extractor.HospitalInfoExtractor // HospitalInfoExtractor: HTML에서 병원 정보를 추출하는 유틸리티 클래스

// Jsoup (HTML 파싱 라이브러리) 관련 import 
import org.jsoup.Jsoup // HTML 문서를 다운로드하고, DOM을 분석하는 라이브러리
import org.jsoup.nodes.Document // Jsoup에서 HTML 문서를 나타내는 객체

// Selenium (웹 자동화) 관련 import
import org.openqa.selenium.* // Selenium의 WebDriver, WebElement 등 포함
import org.openqa.selenium.chrome.ChromeDriver // Chrome 브라우저 드라이버
import org.openqa.selenium.chrome.ChromeOptions // Chrome 드라이버 옵션 설정
import org.openqa.selenium.support.ui.ExpectedConditions // 특정 조건이 만족할 때까지 대기하는 기능
import org.openqa.selenium.support.ui.WebDriverWait // 웹 페이지 로딩을 기다리는 기능
import java.time.Duration // Selenium의 대기 시간을 설정하기 위한 클래스

// Spring 관련 import 
import org.springframework.stereotype.Component // 해당 클래스의 Spring의 빈(Bean)으로 등록하는 어노테이션

// JSON 변환 관련 import
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper // Jackson 라이브러리 (JSON 변환 기능 제공)

@Component
class HospitalCrawler(
    private val webCrawler: WebCrawler, // Selenium 기반 웹 크롤러
    private val hospitalInfoExtractor: HospitalInfoExtractor // HTML에서 병원 정보를 추출하는 유틸리티
) {
    // 병원 목록(이름 + URL) 크롤링
    fun crawlHospitalLinks(): List<Pair<String, String>> {
        val hospitalLinks = mutableListOf<Pair<String, String>>() // 병원 목록 저장 리스트
        val driver = webCrawler.createWebDriver() // WebDriver 생성
        try {
            var pageNum = 1 // 크롤링할 페이지 번호
    
            while (true) {
                // 병원 검색 결과 페이지 URL (페이지 번호에 따라 변경됨)
                val url = "https://mobile.hidoc.co.kr/find/result/list?page=$pageNum&filterType=H"
                driver.get(url) // 해당 페이지로 이동
    
                // 최대 20초 동안 요소 로딩 대기
                val wait = WebDriverWait(driver, Duration.ofSeconds(20))
                // 병원 리스트가 로드될 때까지 대기
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.item_search")))

                // 페이지 내 자동 스크롤을 실행하여 추가 데이터 로드
                val jsExecutor = driver as JavascriptExecutor
                for (i in 1..5) {
                    // 페이지 맨 아래로 스크롤
                    jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);")
                    Thread.sleep(3000) // 데이터 로딩을 위한 대기 (3초)
                }
    
                // 병원 목록이 있는 요소 찾기
                val hospitalElements: List<WebElement> = driver.findElements(By.cssSelector("div.item_search a.link_item"))
    
                // 병원 데이터가 없을 경우 재시도
                if (hospitalElements.isEmpty()) {
                    println("🚨 No hospital data found (page: $pageNum). Retrying...") // 병원 데이터가 없을 경우 로그 출력
                    Thread.sleep(5000) // 5초 대기 후 재시도
                    continue
                }
                
                // 병원 이름 및 URL 추출
                for (element in hospitalElements) {
                    // 병원 이름 추출 (없으면 "이름 없음")
                    val name = element.findElement(By.tagName("strong")).text ?: "이름 없음"

                    // 병원 상세 페이지 URL (상대경로)
                    val relativeLink = element.getAttribute("href") ?: ""
                    
                    // 절대 URL로 변환
                    val fullLink = if (relativeLink.startsWith("/")) "https://mobile.hidoc.co.kr$relativeLink" else relativeLink
                    
                    // 병원 이름과 URL을 리스트에 추가
                    hospitalLinks.add(name to fullLink)
                }
                
                // 크롤링된 병원 수 출력
                println("✅ Page $pageNum - Found ${hospitalElements.size} hospital links (Total: ${hospitalLinks.size})")
                
                /* 
                // 전체 페이지 크롤링을 원할 경우 아래 코드로 변경
                if (hospitalElements.isEmpty()) {
                    println("No more hospital data found. Stopping at page $pageNum.")
                    break
                }
                */
                if (pageNum >= 1) break // 최대 1페이지까지만 크롤링
               
                pageNum++ // 다음 페이지로 이동
            }
        } catch (e: Exception) { // 오류 발생 시 출력
            println("⚠️ Error in crawlHospitalLinks: ${e.message}")
        } finally { // WebDriver 종료
            driver.quit()
        }
        
        return hospitalLinks // 병원 목록 반환
    }
    
    // 병원 상세 정보 크롤링 (이름, 전화번호, 주소, 진료과목 등)
    fun crawlHospitalInfos(name: String, url: String): Map<String, Any?> {
        val driver = webCrawler.createWebDriver() // WebDriver 생성
        var hospitalId: String? // 병원 ID 저장 변수

        return try {
            driver.get(url) // 병원 상세 페이지 접속

            // 최대 10초 동안 페이지 로딩 대기
            val wait = WebDriverWait(driver, Duration.ofSeconds(10))

            // 페이지 로딩 완료 확인
            wait.until { (driver as JavascriptExecutor).executeScript("return document.readyState") == "complete" }

            // 현재 페이지의 HTML 소스 가져오기
            val htmlContent = driver.pageSource ?: ""

            // Jsoup을 사용하여 HTML 문서 파싱
            val doc: Document = Jsoup.parse(htmlContent)

            // URL에서 병원 ID 추출
            hospitalId = url.substringAfterLast("/")

            // 병원 전화번호 추출
            val phoneNumber = hospitalInfoExtractor.extractPhoneNumber(doc) ?: ""
            
            // 병원 홈페이지 URL 추출
            val homepage = hospitalInfoExtractor.extractHomepage(doc) ?: ""

            // 병원 주소 추출
            val address = hospitalInfoExtractor.extractAddress(doc) ?: ""

            // 병원 진료과목 추출
            val specialties = hospitalInfoExtractor.extractSpecialties(doc) ?: ""

            // 병원의 운영 시간 정보 추출 (JSON 변환)
            val operatingHoursMap = hospitalInfoExtractor.extractOperatingHours(doc) // 운영 시간 정보 추출
            val operatingHours = jacksonObjectMapper().writeValueAsString(operatingHoursMap ?: emptyMap<String, String>()) // JSON 변환

            // 병원 추가 정보 추출 (24시간 응급실 여부, 협진 시스템 등)
            val additionalInfo = hospitalInfoExtractor.extractAdditionalInfo(doc, hospitalId) ?: "" 

            // 병원에 소속된 의사 정보 크롤링 (의사 ID 및 프로필 URL)
            val doctorUrls = hospitalInfoExtractor.extractDoctorUrls(doc) // 의사 정보 추출
            val doctorUrlsJson = jacksonObjectMapper().writeValueAsString(doctorUrls) // JSON 변환

            // 크롤링한 병원 정보를 Map 형태로 변환 
            mapOf(
                "hospital_id" to hospitalId, // 병원 ID
                "name" to name, // 병원 이름
                "phone_number" to phoneNumber, // 병원 전화번호
                "homepage" to homepage, // 병원 홈페이지 URL
                "address" to address, // 병원 주소
                "specialties" to specialties, // 병원 진료과 정보
                "operating_hours" to operatingHours, // 병원 운영 시간 (JSON 변환)
                "additional_info" to additionalInfo, // 병원 추가 정보 
                "doctor_urls" to doctorUrlsJson, // 병원 소속 의사 정보 (JSON 변환)
                "url" to url // 병원 상세 페이지 URL
            )
        } catch (e: Exception) {
            // 크롤링 중 오류 발생 시 로그 출력 및 기본 응답 반환
            println("⚠️ Failed to crawl hospital info from $url: ${e.message}")
            mapOf(
                "hospital_id" to (hospitalId ?: ""), // 병원 ID (없으면 빈 값)
                "name" to name, // 병원 이름
                "url" to url, // 병원 상세 페이지 URL
                "error" to "⚠️ ${e.message}" // 오류 메시지 포함
            )
        } finally {
            driver.quit() // WebDriver 종료
        }
    }
}
