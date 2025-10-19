package com.callrapport.component.crawler.doctor

// Component 및 서비스 관련 import
import com.callrapport.component.crawler.WebCrawler
// Jsoup (HTML 파싱 라이브러리) 관련 import
import org.jsoup.Jsoup
// Selenium (웹 자동화) 관련 import
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration
// Spring 관련 import
import org.springframework.stereotype.Component

@Component
class FamousDoctorCrawler(
    private val webCrawler: WebCrawler
) {
    private val BASE_URL = "https://health.chosun.com/bestdoctor/bestdoctor_list_ds.jsp"

    // --- [참고용] 우리 DB의 표준 진료과 목록 (하이닥 기반) ---
    private val CANONICAL_SPECIALTY_NAMES = setOf(
        "가정의학과", "내과", "마취통증의학과", "방사선종양학과", "병리과", "비뇨의학과", "산부인과", 
        "산업의학과", "성형외과", "소아청소년과", "신경과", "신경외과", "안과", "영상의학과", 
        "예방의학과", "외과", "응급의학과", "이비인후과", "재활의학과", "정신건강의학과", 
        "정형외과", "직업환경의학과", "진단검사의학과", "치과", "피부과", "한방과", 
        "핵의학과", "흉부외과"
    )

    // --- [핵심 매핑 테이블] 크롤링된 세부 진료과를 우리 표준 진료과로 매핑 ---
    private val CRAWLED_TO_CANONICAL_MAP = mapOf(
        "간담췌외과" to "외과", "소화기외과" to "외과", "위장관외과" to "외과", "대장항문외과" to "외과",
        "일반외과" to "외과", "유방내분비외과" to "외과", "종양내과" to "내과", "소화기내과" to "내과",
        "신장내과" to "내과", "알레르기내과" to "내과", "호흡기내과" to "내과", "혈액종양내과" to "내과",
        "내분비내과" to "내과", "순환기내과" to "내과", "심장내과" to "내과", "비뇨기과" to "비뇨의학과",
        "심장외과" to "흉부외과", "혈관외과" to "흉부외과", "갑상선내분비외과" to "내분비외과",
        "이식외과" to "외과", "치의학과" to "치과", "기타" to "한방과", "심혈관내과" to "내과",
        "류마티스내과" to "내과", // 추가된 매핑
        "감염내과" to "내과"      // 추가된 매핑
    )

    /**
     * 크롤링된 진료과 이름을 우리 DB의 표준 진료과 이름으로 매핑합니다.
     */
    private fun mapSpecialtyName(originalName: String): String {
        val trimmedName = originalName.trim()
        
        // 1. 명시적 매핑 테이블 확인 (가장 정확한 매핑)
        val mappedName = CRAWLED_TO_CANONICAL_MAP[trimmedName] 
        if (mappedName != null) return mappedName
        
        // 2. 표준 진료과 이름과 부분 일치 확인
        return CANONICAL_SPECIALTY_NAMES.find { 
            trimmedName.contains(it) || it.contains(trimmedName) 
        } ?: trimmedName // 매핑이 안되면 원본 이름을 그대로 반환
    }
    
    /**
     * 명의 목록을 크롤링하여 Map의 리스트 형태로 반환하는 함수
     * @return 크롤링된 명의 정보(Map)의 목록
     */
    fun crawlFamousDoctors(): List<Map<String, String>> {
        val driver = webCrawler.createWebDriver()
        val wait = WebDriverWait(driver, Duration.ofSeconds(10))
        val famousDoctorsData = mutableListOf<Map<String, String>>()

        try {
            driver.get(BASE_URL)
            println("Page loaded successfully: $BASE_URL")

            // data-cate 속성은 <div>에 있으므로, <div> 요소를 수집하고 그 안에서 <span>을 찾습니다.
            val categoryDivs = driver.findElements(By.cssSelector("nav#demo div.icon_cate"))
            val categoryInfos = categoryDivs.mapNotNull { divElement ->
                val categoryId = divElement.getAttribute("data-cate")
                val categoryName = try {
                    divElement.findElement(By.tagName("span")).text
                } catch (e: NoSuchElementException) {
                    null
                }

                if (categoryId != null && categoryName != null) {
                    categoryId to categoryName
                } else {
                    null
                }
            }

            // --- [크롤링 범위 제어] ---
            // [전체 실행 모드] 모든 카테고리를 크롤링합니다.
            val targetCategories = categoryInfos

            // [테스트 모드] 1개의 카테고리만 크롤링하려면, 위 줄을 주석 처리하고 아래 줄의 주석을 해제하세요.
            // val targetCategories = categoryInfos.take(1)
            // ------------------------

            println("--- Crawling ${targetCategories.size} category(s). ---")

            for ((categoryId, categoryName) in targetCategories) {
                try {
                    val categoryUrl = "$BASE_URL?sel_disease_top=$categoryId"
                    println("\n>>> Processing Category: '$categoryName' by navigating to URL: $categoryUrl")
                    driver.get(categoryUrl)
                    
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.doctor-card")))
                    println("Category content loaded.")
                    
                    while (true) {
                        try {
                            val showMoreSpan = driver.findElement(By.id("show_more"))
                            if (showMoreSpan.text != "명의 더보기") {
                                break
                            }
                            val moreButton = showMoreSpan.findElement(By.xpath("./.."))
                            (driver as JavascriptExecutor).executeScript("arguments[0].click();", moreButton)
                            println("Clicking 'Show More'...")
                            Thread.sleep(1500)
                        } catch (e: NoSuchElementException) {
                            break
                        }
                    }

                    val doc = Jsoup.parse(driver.pageSource)
                    val doctorCards = doc.select("a.doctor-card")
                    println(">>> Found a total of ${doctorCards.size} doctors in this category.")

                    for (card in doctorCards) {
                        val name = card.selectFirst("h3.dc_list_name")?.ownText()?.trim() ?: continue
                        val hospitalName = card.selectFirst("span.hopital_name")?.text()?.trim() ?: continue
                        val specialtyRaw = card.selectFirst("span.part_name")?.text()?.trim() ?: continue

                        // --- [핵심 수정: 매핑 로직] ---
                        val mappedSpecialties = specialtyRaw.split(",")
                            .map { mapSpecialtyName(it) } // 각 진료과를 매핑
                            .joinToString(",") // 다시 쉼표로 연결
                        // ------------------------------
                        
                        val doctorData = mapOf(
                            "name" to name,
                            "hospitalName" to hospitalName,
                            // 매핑된 진료과 이름을 반환
                            "specialtyName" to mappedSpecialties, 
                            "categoryName" to categoryName
                        )
                        famousDoctorsData.add(doctorData)
                        println("Found Doctor -> $doctorData")
                    }
                } catch (e: Exception) {
                    println("⚠️ Error processing Category ID '$categoryId': ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("⚠️ Error during the crawling process: ${e.message}")
        } finally {
            driver.quit()
            println("Web driver has been closed.")
        }

        println("Crawling finished. Returning data for ${famousDoctorsData.size} doctors.")
        return famousDoctorsData
    }
}
