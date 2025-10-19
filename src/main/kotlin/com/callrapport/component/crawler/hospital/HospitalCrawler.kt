package com.callrapport.component.crawler.hospital

// WebCrawler 및 병원 정보 추출기 관련 import
import com.callrapport.component.crawler.WebCrawler // WebCrawler: Selenium을 이용한 웹 크롤링 기능 제공
import com.callrapport.component.extractor.HospitalInfoExtractor // HospitalInfoExtractor: HTML에서 병원 정보를 추출하는 유틸리티 클래스

// Jsoup (HTML 파싱 라이브러리) 관련 import 
import org.jsoup.Jsoup // HTML 문서를 다운로드하고, DOM을 분석하는 라이브러리
import org.jsoup.nodes.Document // Jsoup에서 HTML 문서를 나타내는 객체

// Repository 관련 import
import com.callrapport.repository.administrativeRegion.SggRepository // 행정구역(Sgg) 관련 데이터베이스 작업을 위한 리포지토리

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

// 병원 상세 정보를 선택적으로 지정하기 위한 enum 클래스
enum class HospitalField {
    ID, NAME, PHONE, HOMEPAGE, ADDRESS, SPECIALTIES, OPERATING_HOURS, ADDITIONAL_INFO, DOCTORS, URL
}

@Component
class HospitalCrawler(
    private val webCrawler: WebCrawler, // Selenium 기반 웹 크롤러
    private val hospitalInfoExtractor: HospitalInfoExtractor // HTML에서 병원 정보를 추출하는 유틸리티
) {
    // 병원 목록(이름 + URL) 크롤링
    fun crawlHospitalLinks(
        area1: String? = null, // 시/도 (예: "충청북도")
        area2: String? = null  // 시/군/구 (예: "청주시 서원구")
    ): List<Pair<String, String>> {
        val hospitalLinks = mutableListOf<Pair<String, String>>()
        val driver = webCrawler.createWebDriver()
        try {
            var pageNum = 1

            while (true) {
                val urlBuilder = StringBuilder("https://mobile.hidoc.co.kr/find/result/list?orderType=15010&filterType=H")

                area1?.let { urlBuilder.append("&area1=$it") }
                area2?.let { urlBuilder.append("&area2=$it") }

                urlBuilder.append("&page=$pageNum")

                val url = urlBuilder.toString()
                println("Crawling URL: $url")
                driver.get(url)

                val wait = WebDriverWait(driver, Duration.ofSeconds(20))
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.item_search")))

                val jsExecutor = driver as JavascriptExecutor
                for (i in 1..5) {
                    jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);")
                    Thread.sleep(3000)
                }

                val hospitalElements: List<WebElement> = driver.findElements(By.cssSelector("div.item_search a.link_item"))

                if (hospitalElements.isEmpty()) {
                    if (pageNum == 1) {
                        println("🚨 No hospital data found for the selected region. Stopping.")
                    } else {
                        println("No more hospital data found. Stopping at page ${pageNum - 1}.")
                    }
                    break // 데이터가 없으면 루프 종료
                }

                for (element in hospitalElements) {
                    val name = element.findElement(By.tagName("strong")).text ?: "이름 없음"
                    val relativeLink = element.getAttribute("href") ?: ""
                    val fullLink = if (relativeLink.startsWith("/")) "https://mobile.hidoc.co.kr$relativeLink" else relativeLink
                    hospitalLinks.add(name to fullLink)
                }

                println("✅ Page $pageNum - Found ${hospitalElements.size} hospital links (Total: ${hospitalLinks.size})")

                // maxPage 관련 if문 제거

                pageNum++
            }
        } catch (e: Exception) {
            println("⚠️ Error in crawlHospitalLinks: ${e.message}")
        } finally {
            driver.quit()
        }

        return hospitalLinks
    }
    
    // 병원 상세 정보 크롤링 (지정된 필드만 추출)
    fun crawlHospitalInfos(
        name: String,
        url: String,
        fields: List<HospitalField> = HospitalField.values().toList() // 기본은 전체 필드
    ): Map<String, Any?> {
        val driver = webCrawler.createWebDriver()
        var hospitalId: String? = null

        return try {
            driver.get(url)
            val wait = WebDriverWait(driver, Duration.ofSeconds(10))
            wait.until { (driver as JavascriptExecutor).executeScript("return document.readyState") == "complete" }

            val htmlContent = driver.pageSource ?: ""
            val doc: Document = Jsoup.parse(htmlContent)

            hospitalId = url.substringAfterLast("/")
            val mapper = jacksonObjectMapper()

            val result = mutableMapOf<String, Any?>()

            if (HospitalField.ID in fields) result["hospital_id"] = hospitalId
            if (HospitalField.NAME in fields) result["name"] = name
            if (HospitalField.PHONE in fields) result["phone_number"] = hospitalInfoExtractor.extractPhoneNumber(doc)
            if (HospitalField.HOMEPAGE in fields) result["homepage"] = hospitalInfoExtractor.extractHomepage(doc)
            if (HospitalField.ADDRESS in fields) result["address"] = hospitalInfoExtractor.extractAddress(doc)
            if (HospitalField.SPECIALTIES in fields) result["specialties"] = hospitalInfoExtractor.extractSpecialties(doc)
            if (HospitalField.OPERATING_HOURS in fields) {
                val hours = hospitalInfoExtractor.extractOperatingHours(doc)
                result["operating_hours"] = mapper.writeValueAsString(hours ?: emptyMap<String, String>())
            }
            if (HospitalField.ADDITIONAL_INFO in fields) {
                result["additional_info"] = hospitalInfoExtractor.extractAdditionalInfo(doc, hospitalId)
            }
            if (HospitalField.DOCTORS in fields) {
                val doctorUrls = hospitalInfoExtractor.extractDoctorUrls(doc)
                result["doctor_urls"] = mapper.writeValueAsString(doctorUrls)
            }
            if (HospitalField.URL in fields) result["url"] = url

            result

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

    fun crawlOperatingHoursFromNaver(url: String): Map<String, String> {
        val driver = webCrawler.createWebDriver()
        val result = mutableMapOf<String, String>()
        
        try {
            driver.get(url)
            
            // 로딩 대기
            WebDriverWait(driver, Duration.ofSeconds(10)).until {
                (driver as JavascriptExecutor).executeScript("return document.readyState") == "complete"
            }
            
            // 현재 페이지 HTML 전체를 저장 (디버깅용)
            val currentHtml = driver.pageSource
            result["탭_HTML"] = currentHtml
            
            val originalHandles = driver.windowHandles.toSet()
            result["탭_개수_이전"] = originalHandles.size.toString()
        
            var movedSuccessfully = false
        
            try {
                driver.findElement(By.id("_title"))
                result["1단계"] = "success"
                movedSuccessfully = true
            } catch (e: NoSuchElementException) {
                val linkElements = driver.findElements(By.cssSelector("a.place_bluelink"))
                val validLink = linkElements.firstOrNull {
                    val href = it.getAttribute("href")
                    href != null && href != "#" && href.isNotBlank()
                }
    
                if (validLink != null) {
                    validLink.click()
                    Thread.sleep(5000)
    
                    val newHandles = driver.windowHandles.toSet()
                    result["탭_개수_이후"] = newHandles.size.toString()
    
                    val diffHandles = newHandles - originalHandles
    
                    if (diffHandles.isNotEmpty()) {
                        val newTab = diffHandles.first()
                        driver.switchTo().window(newTab)
    
                        // 새 탭에서 div#_pcmap_list_scroll_container 내에 ul 태그가 있는지 확인
                        try {
                            val ulElement = driver.findElement(By.cssSelector("div#_pcmap_list_scroll_container ul"))
                            if (ulElement != null) {
                                result["탭_전환_성공"] = "success"
                            } else {
                                result["탭_전환_성공"] = "failed"
                            }
                        } catch (e: NoSuchElementException) {
                            result["탭_전환_성공"] = "failed"
                        }
    
                        try {
                            driver.findElement(By.id("_title"))
                            result["1단계"] = "success"
                            movedSuccessfully = true
                        } catch (e: NoSuchElementException) {
                            result["1단계"] = "failed"
                            result["error"] = "❌ 새 탭 전환은 성공했으나 '_title' 요소를 찾지 못함"
                        }
                    } else {
                        result["1단계"] = "failed"
                        result["error"] = "✅ 링크 클릭됨. 하지만 새 탭이 열리지 않음 (탭 개수 동일)"
                    }
                } else {
                    result["1단계"] = "failed"
                    result["error"] = "❌ 'place_bluelink' 요소 클릭 불가 (유효한 href 없음)"
                }
            }
        
            if (!movedSuccessfully) {
                result["error"] = result["error"] ?: "⚠️ '_title' 탐색 실패 및 탭 전환 실패"
                result["탭_개수_이후"] = driver.windowHandles.size.toString()
            }
        
        } catch (e: Exception) {
            result["1단계"] = result["1단계"] ?: "failed"
            result["error"] = "⚠️ 예외 발생: ${e.message}"
        } finally {
            driver.quit()
        }
        
        return result
    }
    
}
