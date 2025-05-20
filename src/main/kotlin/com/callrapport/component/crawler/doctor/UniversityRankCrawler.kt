package com.callrapport.component.crawler.doctor

// Spring 컴포넌트로 등록
import org.springframework.stereotype.Component

// WebDriver 제공 컴포넌트
import com.callrapport.component.crawler.WebCrawler

// Selenium 관련 import
import org.openqa.selenium.WebDriver

@Component
class UniversityRankCrawler(
    private val webCrawler: WebCrawler // 웹 드라이버 생성 유틸리티
) {

    fun startCrawling() {
        val driver: WebDriver = webCrawler.createWebDriver() // 설정된 Selenium 드라이버 생성

        try {
            // 타겟 URL: THE 2025 의과대학 순위 페이지
            val url = "https://www.timeshighereducation.com/world-university-rankings/2025/subject-ranking/clinical-pre-clinical-health"
            driver.get(url)

            println("🌐 THE 순위 페이지 접속 완료: $url")

            // 이 이후에 실제 데이터를 Jsoup.parse(driver.pageSource)로 DOM 파싱해서 분석하면 됨

        } catch (e: Exception) {
            println("❌ 크롤링 중 오류 발생: ${e.message}")
        } finally {
            driver.quit()
        }
    }
}
