package com.callrapport.component.crawler

import com.callrapport.component.crawler.WebCrawler // Selenium 기반의 WebDriver 생성기
import com.callrapport.component.extractor.DiseaseInfoExtractor // HTML 문서에서 질병 정보 추출

// Jsoup 관련 라이브러리 (HTML 파싱 및 데이터 추출)
import org.jsoup.Jsoup // HTML 문서 다운로드 및 파싱
import org.jsoup.nodes.Document // HTML 문서를 나타내는 객체

// Selenium 관련 라이브러리 (동적 HTML 크롤링)
import org.openqa.selenium.By // 요소를 찾기 위한 클래스
import org.openqa.selenium.JavascriptExecutor // JavaScript 실행을 위한 인터페이스
import org.openqa.selenium.WebDriver // 웹 브라우저 조작을 위한 인터페이스 
import org.openqa.selenium.support.ui.WebDriverWait // 웹 페이지 로딩을 기다리는 기능
import org.openqa.selenium.support.ui.ExpectedConditions // 특정 조건이 만족할 때까지 대기하는 기능

// Spring 및 JSON 관련 라이브러리
import org.springframework.stereotype.Component // Spring Bean으로 등록하기 위한 어노테이션

// 기타 유틸리티 라이브러리
import java.time.Duration // WebDriver 대기 시간 설정

@Component
class DiseaseCrawler(
    private val webCrawler: WebCrawler, // Selenium WebDriver를 생성하는 클래스
    private val diseaseInfoExtractor: DiseaseInfoExtractor // HTML에서 질병 정보를 추출하는 클래스 
) {
    // 질병 정보로 이동하는 링크들을 수집
    fun crawlDiseaseLinks(): List<Pair<String, String>> {
        val driver = webCrawler.createWebDriver() // WebDriver 생성
        val diseaseLinks = mutableListOf<Pair<String, String>>() // 질병 링크를 저장할 리스트
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
                    .mapNotNull { element -> 
                        val regex = """fn_goView\('(\d+)','(.*?)'\)""".toRegex()
                        val matchResult = regex.find(element.attr("href"))
                        matchResult?.let { result ->
                            val id = result.groupValues[1] // 질병 ID
                            val name = result.groupValues[2] // 질병명
                            val link = "https://health.kdca.go.kr/healthinfo/biz/health/gnrlzHealthInfo/gnrlzHealthInfo/gnrlzHealthInfoView.do?cntnts_sn=$id"
                            name to link // (병명, 링크) 쌍으로 저장
                        }
                    }
                diseaseLinks.addAll(links) // 추출한 href 값을 리스트에 추가 

                println("$pageNum page") // 현재 페이지 번호 출력
                println("new found disease links count: ${links.size}") // 새롭게 발견된 링크 개수 출력
                println("total found disease links count: ${diseaseLinks.size}") // 전체 발견된 링크 개수 출력

                val nextPageElement = document.select("a[onclick^=fn_submit]").firstOrNull { element ->
                    element.attr("onclick").contains("fn_submit(${pageNum + 1})")
                } // 다음 페이지로 이동할 링크 찾기
                
                if (nextPageElement == null) { // 다음 페이지가 없다면 크롤링 종료
                    println("✅ disease crawling completed")
                    break
                } else { // 다음 페이지가 있으므로 크롤링 계속
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
            listOf("Error" to "⚠️ disease links crawling error occurred: ${e.message}")
        } finally {
            driver.quit()
        }
    }

    // 단일 질병 정보를 크롤링
    fun crawlDiseaseInfos(name: String, url: String): Map<String, String?> {
        return try {
            val doc: Document = Jsoup.connect(url).get()

            mapOf(
                "name" to name, // 질병명
                "overview" to diseaseInfoExtractor.extractOverview(doc), // 질병 요약
                "definition" to diseaseInfoExtractor.extractDefinition(doc), // 정의
                "type" to diseaseInfoExtractor.extractType(doc), // 질병의 분류/유형
                "cause" to diseaseInfoExtractor.extractCause(doc), // 원인
                "symptoms" to diseaseInfoExtractor.extractSymptoms(doc), // 증상
                "diagnosis" to diseaseInfoExtractor.extractDiagnosis(doc), // 진단 방법
                "progress" to diseaseInfoExtractor.extractProgress(doc), // 경과/예후
                "pathophysiology" to diseaseInfoExtractor.extractPathophysiology(doc), // 병태생리
                "treatment" to diseaseInfoExtractor.extractTreatment(doc), // 전체적인 치료 정보
                "drug_treatment" to diseaseInfoExtractor.extractDrugTreatment(doc), // 약물 치료
                "non_drug_treatment" to diseaseInfoExtractor.extractNonDrugTreatment(doc), // 비약물 치료
                "self_care" to diseaseInfoExtractor.extractSelfCare(doc), // 자가 관리법
                "self_diagnosis" to diseaseInfoExtractor.extractSelfDiagnosis(doc), // 자가 진단
                "when_to_visit_hospital" to diseaseInfoExtractor.extractWhenToVisitHospital(doc), // 병원에 방문해야 하는 경우
                "related_diseases" to diseaseInfoExtractor.extractRelatedDiseases(doc), // 관련 질병
                "related_symptoms" to diseaseInfoExtractor.extractRelatedSymptoms(doc), // 관련 증상
                "complications" to diseaseInfoExtractor.extractComplications(doc), // 합병증
                "custom_made_info" to diseaseInfoExtractor.extractCustomMadeInfo(doc), // 맞춤 정보
                "related_keywords" to diseaseInfoExtractor.extractRelatedKeywords(doc), // 연관 키워드 
                "prevention" to diseaseInfoExtractor.extractPrevention(doc), // 예방 방법
                "FAQ" to diseaseInfoExtractor.extractFAQ(doc), // 자주 묻는 질문
                "references" to diseaseInfoExtractor.extractReferences(doc), // 참고 문헌
                "url" to url // 원본 페이지 URL
            )
        } catch (e: Exception) {
            // 예외 발생시 에러 메시지를 콘소레 출력 (URL과 함께 표시)
            println("⚠️ Failed to crawl disease info from ${url}: ${e.message}")

            // 오류 발생 시 최소 정보만 담은 Map 반환
            mapOf(
                "name" to name, // 질병명
                "url" to url, // 크롤링 시도한 원본 페이지 URL 
                "error" to "⚠️ ${e.message}" // 발생한 예외 메시지를 포함한 오류 정보
            )
        }
    }
}
