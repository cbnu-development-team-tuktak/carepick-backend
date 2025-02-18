package com.callrapport.component.crawler

import com.callrapport.component.crawler.WebCrawler 
import com.callrapport.model.disease.Disease // Disease 모델

// Jsoup 관련 라이브러리 (HTML 파싱 및 데이터 추출)
import org.jsoup.Jsoup // HTML 문서 다운로드 및 파싱
import org.jsoup.nodes.Document // HTML 문서를 나타내는 객체
import org.jsoup.nodes.Element // 개별 HTML 요소를 나타내는 객체

// Selenium 관련 라이브러리 (동적 HTML 크롤링)
import org.openqa.selenium.By // 요소를 찾기 위한 클래스
import org.openqa.selenium.JavascriptExecutor // JavaScript 실행을 위한 인터페이스
import org.openqa.selenium.WebDriver // 웹 브라우저 조작을 위한 인터페이스 
import org.openqa.selenium.support.ui.WebDriverWait // 웹 페이지 로딩을 기다리는 기능
import org.openqa.selenium.support.ui.ExpectedConditions // 특정 조건이 만족할 때까지 대기하는 기능

// Spring 및 JSON 관련 라이브러리
import org.springframework.stereotype.Component

// 기타 유틸리티 라이브러리
import java.time.Duration 

@Component
class DiseaseCrawler(
    private val webCrawler: WebCrawler
) {
    fun getDiseaseLinks(): List<String> {
        val driver = webCrawler.createWebDriver() // WebDriver 생성
        val diseaseLinks = mutableListOf<String>() // 질병 링크를 저장할 리스트
        var pageNum = 1 // 현재 페이지 번호

        return try {
            // 질병관리청 국가건강정보포털 링크
            val url = "https://health.kdca.go.kr/healthinfo/biz/health/gnrlzHealthInfo/gnrlzHealthInfo/gnrlzHealthInfoMain.do"
            driver.get(url) // 웹사이트 접속
            
            // 페이지 로드 대기 (최대 10초)
            val wait = WebDriverWait(driver, Duration.ofSeconds(10))
            // body 태그가 로드될 때까지 대기
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")))

            // "건강문제" 버튼을 클릭하여 질병 관련 정보만을 표시
            val healthIssueBtn = driver.findElement(By.id("lclasSn1"))
            healthIssueBtn.click() 
            Thread.sleep(2000) // 2초 정도 페이지가 업데이트되기를 대기 

            while (true) {
                val htmlContent = driver.pageSource // Javascript가 렌더링한 HTML 문서
                val document: Document = Jsoup.parse(htmlContent) // Jsoup으로 HTML 파싱
                
                // 질병 상세 정보 페이지 링크 수집
                val links = document.select("a[href^=javascript:fn_goView]")
                    // 체크 아이콘이 있는 링크만 필터링 (외부 사이트로 이동하지 않는 링크만 필터링)
                    .filter { element -> element.selectFirst("i.xi-check-circle-o") != null }
                    .map { element -> element.attr("href") } // href 속성값을 추출

                diseaseLinks.addAll(links) // 추출한 href 값을 리스트에 추가 

                println("$pageNum page") // 현재 페이지 번호 출력
                println("new found disease link count: ${links.size}") // 새롭게 발견된 링크 개수 출력
                println("total found disease link count: ${diseaseLinks.size}") // 전체 발견된 링크 개수 출력

                val nextPageElement = document.select("a[onclick^=fn_submit]").firstOrNull { element ->
                    element.attr("onclick").contains("fn_submit(${pageNum + 1})")
                } // 다음 페이지로 이동할 링크 찾기
                
                if (nextPageElement == null) { // 다음 페이지가 없다면 크롤링 종료
                    println("✅ disease crawling completed")
                    break
                } else { // 다음 페이지가 있으므로 크롤링 계속(continue)
                    val nextPageNum = pageNum + 1
                    println("go to next page: $nextPageNum") // 이동하는 페이지 번호 출력
                    (driver as JavascriptExecutor).executeScript("fn_submit($nextPageNum);") // Javascript로 페이지 이동 실행
                    Thread.sleep(2000) // 페이지 로드 대기
                    pageNum = nextPageNum // 페이지 번호 업데이트
                }
            }
            diseaseLinks // 크롤링 완료된 질병 링크 목록 반환
        } catch (e: Exception) {
            println("⚠️ disease links crawling error occurred: ${e.message}")
            listOf("⚠️ disease links crawling error occurred: ${e.message}")
        } finally {
            driver.quit()
        }
    }
}
