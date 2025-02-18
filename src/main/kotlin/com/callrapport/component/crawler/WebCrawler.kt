    package com.callrapport.component.crawler

    // Selenium 관련 라이브러리 (동적 HTML 크롤링)
    import org.openqa.selenium.By // 요소를 찾기 위한 클래스
    import org.openqa.selenium.WebDriver // 웹 브라우저 조작을 위한 인터페이스 
    import org.openqa.selenium.WebElement // HTML 요소를 나타내는 객체
    import org.openqa.selenium.chrome.ChromeDriver // Chrome 브라우저를 제어하는 드라이버 
    import org.openqa.selenium.chrome.ChromeOptions // Chrome 실행 옵션 설정
    import org.openqa.selenium.support.ui.WebDriverWait // 웹 페이지 로딩을 기다리는 기능
    import org.openqa.selenium.support.ui.ExpectedConditions // 특정 조건이 만족할 때까지 대기하는 기능

    // Jsoup 관련 라이브러리 (정적 HTML 크롤링)
    import org.jsoup.Jsoup // HTML 파싱 및 문서 가져오기 
    import org.jsoup.nodes.Document // HTML 문서를 나타내는 객체
    import org.jsoup.nodes.Element // HTML 요소를 나타내는 객체

    // Spring 및 JSON 관련 라이브러리
    import org.springframework.stereotype.Component 
    import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

    // 기타 유틸리티 라이브러리
    import java.time.Duration

@Component
class WebCrawler {
    // 웹 드라이버 생성
    fun createWebDriver(): WebDriver {
        System.setProperty("webdriver.chrome.driver", "drivers/chromedriver.exe") // Chrome 드라이버 경로 설정
        val options = ChromeOptions()
        options.addArguments("--headless") // GUI 없이 실행 (백그라운드 동작)
        options.addArguments("--disable-gpu") // GPU 사용 비활성화 (호환성 문제 방지)
        options.addArguments("--no-sandbox") // 샌드박스 모드 비활성화 (권한 문제 방지)
        options.addArguments("--disable-dev-shm-usage") // 메모리 사용 최적화
        return ChromeDriver(options)
    }

    // 전체 HTML 반환
    fun fetchHtml(url: String): String {
        val driver = createWebDriver()
        return try {
            driver.get(url)

            // 페이지 로드 대기 (최대 10초)
            val wait = WebDriverWait(driver, Duration.ofSeconds(10))
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")))

            val htmlContent = driver.pageSource // JavaScript가 렌더링한 HTML 문서
            println("🌐 ${url} HTML get success")
            htmlContent ?: ""
        } catch (e: Exception) {
            println("⚠️ ${url} HTML get failed: ${e.message}")
            "⚠️ HTML get failed: ${e.message}"
        } finally {
            driver.quit() // WebDriver 종료 (리소스 해제)
        }
    }

    // 특정 택스트를 포함한 HTML 반환 
    fun fetchHtmlWithTextContent(url: String, textContent: String): String {
        val driver = createWebDriver()
        return try {
            driver.get(url)

            // 페이지 로드 대기 (최대 10초)
            val wait = WebDriverWait(driver, Duration.ofSeconds(10))
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")))

            val htmlContent = driver.pageSource // JavaScript가 렌더링한 HTML 가져오기
            val document: Document = Jsoup.parse(htmlContent) // HTML을 Jsoup으로 파싱

            // 텍스트를 포함한 요소를 필터링
            val elementsContainingText: List<Element> = document.allElements.filter { element ->
                val elementText = element.ownText().trim()
                elementText.contains(textContent, ignoreCase = true)
            }
            
            // 텍스트를 포함한 요소가 없다면, 경고 안내문을 반환
            if (elementsContainingText.isEmpty()) {
                println("⚠️ No elements found containing $textContent")
                "⚠️ No elements found containing $textContent"
            } 

            // 텍스트를 포함한 요소 개수 출력
            println("✅ Found ${elementsContainingText.size} elements containing '$textContent'")
            
            // 주요 정보를 파악하기 위해 JSON 형식으로 변환
            val tagData = elementsContainingText.map { element ->
                val tagName: String = element.tagName() // 태그명
                val elementText: String = element.ownText() // 텍스트 내용
                val className: String = element.className() // 클래스명
                val elementId: String = element.id() // 고유 ID
                val parentTag: String = element.parent()?.tagName() ?: "" // 부모 태그
                val outerHtmlContent: String = element.outerHtml() // 해당 태그의 전체 HTML (자식 요소 포함)

                mapOf(
                    "tag" to tagName, // 태그명
                    "text" to elementText, // 텍스트 내용
                    "class" to className, // 클래스명
                    "id" to elementId, // 태그 ID
                    "parent_tag" to parentTag, // 부모 태그명
                    "html" to outerHtmlContent // 전체 HTML 내용
                )
            }

            val objectMapper = jacksonObjectMapper()
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tagData)
        } catch (exception: Exception) {
            println("⚠️ ${url} HTML text search failed: ${exception.message}")
            "⚠️ HTML text search failed: ${exception.message}"
        } finally {
            driver.quit()
        }
    }
}