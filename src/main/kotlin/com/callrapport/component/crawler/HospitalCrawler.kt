package com.callrapport.component.crawler

import com.callrapport.component.crawler.WebCrawler 
import com.callrapport.component.extractor.HospitalInfoExtractor

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.openqa.selenium.*
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.stereotype.Component
import java.time.Duration
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper // ✅ Jackson 라이브러리 임포트

@Component
class HospitalCrawler(
    private val webCrawler: WebCrawler,
    private val hospitalInfoExtractor: HospitalInfoExtractor
) {
    fun crawlHospitalLinks(): List<Pair<String, String>> {
        val hospitalLinks = mutableListOf<Pair<String, String>>()
        val driver = webCrawler.createWebDriver() // WebDriver 생성
        try {
            var pageNum = 1
    
            while (true) {
                val url = "https://mobile.hidoc.co.kr/find/result/list?page=$pageNum&filterType=H"
                driver.get(url)
    
                val wait = WebDriverWait(driver, Duration.ofSeconds(20))
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.item_search")))

                val jsExecutor = driver as JavascriptExecutor
                for (i in 1..5) {
                    jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);")
                    Thread.sleep(3000) // ✅ 데이터 로딩 대기
                }
    
                // ✅ 요소가 로드되었는지 재확인
                val hospitalElements: List<WebElement> = driver.findElements(By.cssSelector("div.item_search a.link_item"))
    
                if (hospitalElements.isEmpty()) {
                    println("🚨 No hospital data found (page: $pageNum). Retrying...")
                    Thread.sleep(5000)
                    continue
                }
    
                for (element in hospitalElements) {
                    val name = element.findElement(By.tagName("strong")).text ?: "이름 없음"
                    val relativeLink = element.getAttribute("href") ?: ""
                    val fullLink = if (relativeLink.startsWith("/")) "https://mobile.hidoc.co.kr$relativeLink" else relativeLink
    
                    hospitalLinks.add(name to fullLink)
                }
    
                println("✅ Page $pageNum - Found ${hospitalElements.size} hospital links (Total: ${hospitalLinks.size})")
    
                if (pageNum >= 1) break // ✅ 최대 5페이지까지만 크롤링
    
                pageNum++
            }
        } catch (e: Exception) {
            println("⚠️ Error in crawlHospitalLinks: ${e.message}")
        } finally {
            driver.quit()
        }
    
        return hospitalLinks
    }
    
    /**
     * 병원 상세 정보 크롤링 (이름, 전화번호, 주소, 진료과목 등)
     */
    fun crawlHospitalInfos(name: String, url: String): Map<String, String?> {
        val driver = webCrawler.createWebDriver() // WebDriver 생성

        var hospitalId: String? = null

        return try {
            driver.get(url)

            val wait = WebDriverWait(driver, Duration.ofSeconds(10))
            wait.until { (driver as JavascriptExecutor).executeScript("return document.readyState") == "complete" }

            val htmlContent = driver.pageSource ?: ""
            val doc: Document = Jsoup.parse(htmlContent)

            hospitalId = url.substringAfterLast("/")
            val phoneNumber = hospitalInfoExtractor.extractPhoneNumber(doc) ?: ""
            val homepage = hospitalInfoExtractor.extractHomepage(doc) ?: ""
            val address = hospitalInfoExtractor.extractAddress(doc) ?: ""
            val specialties = hospitalInfoExtractor.extractSpecialties(doc) ?: ""

            // ✅ 운영 시간(JSON 형식으로 변환)
            val operatingHoursMap = hospitalInfoExtractor.extractOperatingHours(doc)
            val operatingHours = jacksonObjectMapper().writeValueAsString(operatingHoursMap ?: emptyMap<String, String>())

            val additionalInfo = hospitalInfoExtractor.extractAdditionalInfo(doc, hospitalId) ?: ""
            
            val doctorIds = hospitalInfoExtractor.extractDoctorIds(doc)
            val doctorIdsJson = jacksonObjectMapper().writeValueAsString(doctorIds) // ✅ JSON 변환

            mapOf(
                "hospital_id" to hospitalId,
                "name" to name,
                "phone_number" to phoneNumber,
                "homepage" to homepage,
                "address" to address,
                "specialties" to specialties,
                "operating_hours" to operatingHours,
                "additional_info" to additionalInfo,
                "doctor_ids" to doctorIdsJson, 
                "url" to url
            )
        } catch (e: Exception) {
            println("⚠️ Failed to crawl hospital info from $url: ${e.message}")
            mapOf(
                "hospital_id" to (hospitalId ?: ""),
                "name" to name,
                "url" to url,
                "error" to "⚠️ ${e.message}"
            )
        } finally {
            driver.quit()
        }
    }
}
