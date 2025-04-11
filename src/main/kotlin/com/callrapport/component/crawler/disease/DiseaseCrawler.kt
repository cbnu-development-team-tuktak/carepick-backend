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
    // 질병명, 링크, 신체계통 정보를 담는 크롤링 결과용 데이터 클래스
    data class DiseaseLinkInfo(
        val name: String, // 질병명 (예: 페렴, 대상포진 등)
        val url: String, // 질병 상세 페이지 URL
        val bodySystem: String // 질병이 속한 신체계통 (예: 호흡기, 피부, 순환기 등)
    )

    // 질병 링크 전체 크롤링
    fun crawlDiseaseLinks(): List<DiseaseLinkInfo> {
        val driver = webCrawler.createWebDriver() // Selenium WebDriver 인스턴스 생성
        val diseaseLinks = mutableListOf<DiseaseLinkInfo>() // 크롤링한 질병 링크 정보를 저장할 목록

        // 신체계통 코드 → 한글 라벨 매핑 (질병 분류 필터용)
        val bodySystemMap = mapOf(
            "NE" to "뇌신경", "JU" to "정신건강", "NU" to "눈",
            "KO" to "귀코목", "KU" to "구강", "BB" to "뼈근육",
            "PB" to "피부", "NB" to "내분비", "HH" to "호흡기",
            "SO" to "순환기", "SW" to "소화기", "MH" to "면역",
            "SA" to "비뇨기", "SS" to "생식기"
        )

        try {
            // 질병관리청 건강정보 메인 페이지 진입
            val url = "https://health.kdca.go.kr/healthinfo/biz/health/gnrlzHealthInfo/gnrlzHealthInfo/gnrlzHealthInfoMain.do"
            driver.get(url) // 해당 URL로 이동

            // 페이지의 <body> 요소가 나타날 때까지 최대 10초 대기
            val wait = WebDriverWait(driver, Duration.ofSeconds(10))
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")))

            // '건강문제' 카테고리 버튼 클릭 (전체 질병 목록이 나타나도록 핕터링)
            val healthIssueBtn = driver.findElement(By.id("lclasSn1"))
            healthIssueBtn.click()
            Thread.sleep(2000) // 페이지가 로드될 시간 확보 (2초 대기)
            
            // 신체계통 코드(code)와 해당 라벨(label)을 하나씩 순회하며 크롤링 수행
            // ex) code: "NE", label: "뇌신경"
            for ((code, label) in bodySystemMap) {
                // 현재 크롤링 중인 카테고리 정보를 로그로 출력 
                println("🔍 Crawling category: [$label] (code: $code)")
            
                try {
                    // 해당 신체계통 카테고리 버튼 클릭 (ex: 뇌신경, 정신건강 등)
                    val categoryBtn = driver.findElement(By.id("bdySystem$code"))
                    categoryBtn.click()
                    println("✅ Successfully clicked category button: [$label]")
                } catch (e: Exception) {
                    // 버튼을 못 찾았거나 클릭에 실패한 경우 로그 출력 후 다음 카테고리로 이동
                    println("❌ Failed to click category button: [$label] - ${e.message}")
                    continue // 다음 카테고리로 넘어감
                }
            
                Thread.sleep(2000) // 카테고리 변경 후 데이터 로딩 대기
            
                var pageNum = 1 // 현재 페이지 번호
                
                // 페이지를 순차적으로 넘기면서 모든 질병 링크를 수집
                while (true) {
                    // 현재 페이지의 렌더링된 HTML 소스를 추출 (Javascript 렌더링 포함)
                    val htmlContent = driver.pageSource

                    // Jsoup을 사용해 HTML 문자열을 Document 객체로 파싱
                    val document: Document = Jsoup.parse(htmlContent)
                    
                    // 'fn_goView' 자바스크립트 함수를 호출하는 링크들을 모두 선택
                    val links = document.select("a[href^=javascript:fn_goView]")
                        // 내부 페이지(체크 표시가 있는 항목)만 선택
                        .filter { element -> element.selectFirst("i.xi-check-circle-o") != null }
                        // 추출된 각 링크에서 질병 ID와 질병명을 파싱
                        .mapNotNull { element ->
                            // href 속성에서 'fn_goView('1234', '추간판탈출증')' 형식의 내용을 정규식으로 추출
                            val regex = """fn_goView\('(\d+?)','(.*?)'\)""".toRegex()
                            val matchResult = regex.find(element.attr("href"))

                            // 정규식 매칭에 성공한 경우, 질병 ID와 이름을 파싱하여 객체를 생성
                            matchResult?.let { result ->
                                val id = result.groupValues[1] // 질병 고유 ID (예: 3348)
                                val name = result.groupValues[2] // 질병 이름 (예: 추간판탈출증)
                                
                                // 상세 페이지 URL을 조합
                                val link = "https://health.kdca.go.kr/healthinfo/biz/health/gnrlzHealthInfo/gnrlzHealthInfo/gnrlzHealthInfoView.do?cntnts_sn=$id"

                                // 질병명, 상세 URL, 신체계통 정보를 하나의 데이터 클래스로 묶어 반환
                                DiseaseLinkInfo(name, link, label)
                            }
                        }
                    
                    // 현재 페이지에서 수집한 질병 링크 수를 콘솔에 출력
                    println("📄 [$label] Page $pageNum: ${links.size} diseases found")

                    // 현재 페이지에서 추출한 링크들을 전체 diseaseLinks 리스트에 추가
                    diseaseLinks.addAll(links)
                        
                    // 다음 페이지 이동 버튼이 있는지 확인
                    // 'onclick' 속성이 fn_submit(다음 페이지 번호)인 a 태그 선택
                    val nextPageElement = document.select("a[onclick^=fn_submit]").firstOrNull { element ->
                        element.attr("onclick").contains("fn_submit(${pageNum + 1})")
                    }
                    
                    // 다음 페이지가 없으면 반복문 종료
                    if (nextPageElement == null) {
                        // 다음 페이지가 없음을 로그 출력
                        println("🚫 [$label] No more pages after page $pageNum")
                        break // 반복문 종료
                    }
                    
                    // 다음 페이지로 이동학 위해 페이지 번호 증가
                    pageNum++

                    // 페이지 이동 로그 출력
                    println("➡️ Moving to next page: $pageNum in [$label]")

                    // JavaScript로 다음 페이지 호출 (fn_submit 함수로 페이지 전환)
                    (driver as JavascriptExecutor).executeScript("fn_submit($pageNum);")

                    // 페이지가 로드될 시간을 확보하기 위해 2초 대기
                    Thread.sleep(2000)
                }
            }
        } catch (e: Exception) {
            // 크롤링 도중 예외가 발생한 경우, 에러 메시지를 로그로 출력
            println("⚠️ disease links crawling error occurred: ${e.message}")
        } finally {
            // WebDriver 사용이 끝났으므로 브라우저 자원 해제
            driver.quit()
        }

        // 전체 수집된 질병 링크 개수를 출력
        println("✅ Total diseases collected: ${diseaseLinks.size}")
        return diseaseLinks // 수집된 질병 링크 목록 반환
    }

    // 단일 질병 정보를 크롤링
    fun crawlDiseaseInfos(
        name: String, // 질병명 (예: 폐렴, 대상포진 등)
        url: String // 질병 상세 페이지 URL
    ): Map<String, String?> { // 크롤링된 질병 정보
        return try {
            // Jsoup을 사용하여 해당 질병 상세 페이지에 접속하고 HTML 문서 파싱
            val doc: Document = Jsoup.connect(url).get()

            mapOf(
                "name" to name, // 질병명
                "symptoms" to diseaseInfoExtractor.extractSymptoms(doc), // 증상
                "url" to url // 원본 페이지 URL
            )
        } catch (e: Exception) {
            // 예외 발생시 에러 메시지를 콘솔에 출력 (URL과 함께 표시)
            println("⚠️ Failed to crawl disease info from $url: ${e.message}")

            // 오류 발생 시 최소 정보만 담은 Map 반환
            mapOf(
                "name" to name, // 질병명
                "url" to url, // 크롤링 시도한 원본 페이지 URL 
                "error" to "⚠️ ${e.message}" // 발생한 예외 메시지를 포함한 오류 정보
            )
        }
    }
}