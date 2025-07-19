package com.callrapport.service.crawl

// 크롤링 및 파일 관련 import
import com.callrapport.component.crawler.WebCrawler // 웹 페이지에서 데이터 크롤링을 수행하는 컴포넌트
import com.callrapport.component.file.FileManager // 파일 저장 및 읽기 처리를 담당하는 유틸리티 클래스

// Jsoup (HTML 파싱 라이브러리) 관련 import
import org.jsoup.Jsoup // HTML 페이지를 파싱하고 DOM 구조를 탐색하는 라이브러리

// Selenium (웹 자동화 라이브러리) 관련 import
import org.openqa.selenium.By // HTML 요소를 탐색하기 위한 선택자 도구
import org.openqa.selenium.JavascriptExecutor // 자바스크립트 실행을 위한 Selenium 인터페이스
import org.openqa.selenium.support.ui.ExpectedConditions // 특정 조건이 충족될 때까지 기다리는 조건 클래스
import org.openqa.selenium.support.ui.WebDriverWait // 요소 로딩을 기다리는 유틸리티

// Spring 및 기타 유틸 관련 import
import org.springframework.stereotype.Service // 해당 클래스를 서비스 컴포넌트로 등록
import java.nio.charset.Charset // 문자 인코딩 설정 클래스
import java.time.Duration // 시간 간격 설정을 위한 클래스 (대기 시간)

@Service
class AdministrativeRegionCrawlService(
    private val webCrawler: WebCrawler, // WebCrawler 의존성 주입
    private val fileManager: FileManager // FileManager 의존성 주입
) {
    fun crawlSidoList(outputFilePath: String = "csv/sido_list.csv") {
        val driver = webCrawler.createWebDriver() // Selenium을 사용한 WebDriver 생성
        val result = mutableListOf<Map<String, String>>() // 결과를 저장할 리스트

        try {
            val url = "https://www.code.go.kr/stdcode/regCodeL.do"
            driver.get(url) // 행정구역 코드 페이지 접속
            println("🌐 Opened 법정동코드 페이지")

            // 최대 10초간 셀렉트 박스 로딩 대기
            val wait = WebDriverWait(driver, Duration.ofSeconds(10))
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Type1")))

            val doc = Jsoup.parse(driver.pageSource) // 페이지 소스를 Jsoup으로 파싱
            val options = doc.select("select#Type1 > option") // 시도 목록 옵션 선택

            var autoCode = 1 // 자동 번호 매기기용 코드 시작값

            for (option in options) {
                val rawCode = option.attr("value").trim() // 사이트에서 제공하는 코드 (사용 안함)
                val name = option.text().trim()

                // 안내용 항목이나 비정상 값은 건너뜀
                if (rawCode == "*" || rawCode.isBlank() || name == "시/도" || name.contains("선택")) continue

                // 시/도 구분
                val type = when {
                    name.contains("도") || name.contains("자치도") -> "도"
                    name.contains("시") || name.contains("특별시") || name.contains("광역시") || name.contains("자치시") -> "시"
                    else -> "기타"
                }

                val code = autoCode.toString().padStart(2, '0') // 2자리 숫자로 포맷팅 (01, 02, ...)

                println("🏙️ [$code] $name ($type)") // 시도 정보 출력

                result.add(
                    mapOf(
                        "code" to code, // 자동 생성된 코드
                        "name" to name, // 시도 이름
                        "type" to type // 시도 타입 (도, 시, 기타
                    )
                )

                autoCode++ // 다음 시도를 위해 자동 코드 증가
            }

            // 결과를 CSV 파일로 저장
            fileManager.writeCsv(outputFilePath, result, charset = Charset.forName("UTF-8"))
            println("✅ Sido list saved to $outputFilePath")

        } catch (e: Exception) {
            println("❌ Error during sido crawl: ${e.message}")
        } finally {
            driver.quit()
        }
    }

    // 시군구 목록을 크롤링하여 CSV 파일로 저장하는 메서드
    fun crawlSggList(outputFilePath: String = "csv/sgg_list.csv") {
        val driver = webCrawler.createWebDriver() // Selenium WebDriver 생성
        val result = mutableListOf<Map<String, String>>() // 결과를 저장할 리스트

        try {
            // 법정동 코드 페이지 열기
            val url = "https://www.code.go.kr/stdcode/regCodeL.do"
            driver.get(url) // 페이지 열기
            println("🌐 Opened 법정동코드 페이지") 

            // 최대 10초간 셀렉트 박스 로딩 대기
            val wait = WebDriverWait(driver, Duration.ofSeconds(10))
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Type1")))

            // 시도 선택 박스에서 옵션 가져오기
            val sidoOptions = driver.findElements(By.cssSelector("select#Type1 > option"))
            var autoCode = 1 // 시군구 코드 자동 생성 시작값

            // 각 시도에 대해 반복
            for (sidoOption in sidoOptions) {
                val sidoCode = sidoOption.getAttribute("value").trim() // 시도 코드
                val sidoName = sidoOption.text.trim() // 시도 이름

                // 시도 코드가 비어있거나 "시/도"인 경우 건너뜀
                if (sidoCode == "*" || sidoCode.isBlank() || sidoName == "시/도") continue

                // 시도 선택 후 시군구 목록 로딩
                println("🏙️ Processing SIDO [$sidoCode] $sidoName")

                // 자바스크립트를 사용하여 시도 선택
                val script = """
                    document.getElementById("Type1").value = "$sidoCode";
                    func_sgg();
                """.trimIndent()
                (driver as JavascriptExecutor).executeScript(script)
                Thread.sleep(1000)

                // 시군구 목록을 Jsoup으로 파싱
                val doc = Jsoup.parse(driver.pageSource)
                val sigunguOptions = doc.select("select#Type2 > option")

                // 각 시군구에 대해 반복
                for (sigunguOption in sigunguOptions) {
                    val rawCode = sigunguOption.attr("value").trim() // 시군구 코드 (사용 안함)
                    val name = sigunguOption.text().trim() // 시군구 이름

                    // 시군구 코드가 비어있거나 "시/군/구"인 경우 건너뜀
                    if (rawCode == "*" || rawCode.isBlank() || name == "시/군/구") continue

                    val type = when {
                        name.endsWith("시") -> "시" // 시군구 이름이 "시"로 끝나면 시
                        name.endsWith("군") -> "군" // 시군구 이름이 "군"으로 끝나면 군
                        name.endsWith("구") -> "구" // 시군구 이름이 "구"로 끝나면 구
                        else -> "기타" // 그 외의 경우 기타
                    }

                    // 시군구 코드 자동 생성 (3자리 숫자)
                    val code = autoCode.toString().padStart(3, '0')

                    println("    🏘️ [$code] $name ($type)")

                    // 결과 리스트에 시군구 정보 추가
                    result.add(
                        mapOf(
                            "parent_name" to sidoName, // 부모 시도의 이름
                            "code" to code, // 자동 생성된 시군구 코드
                            "name" to name, // 시군구 이름
                            "type" to type // 시군구 타입 (시, 군, 구, 기타
                        )
                    )

                    autoCode++ // 다음 시군구를 위해 자동 코드 증가
                }
            }

            // 결과를 CSV 파일로 저장
            fileManager.writeCsv(outputFilePath, result, charset = Charset.forName("UTF-8"))
            println("✅ Sigungu list saved to $outputFilePath")

        } catch (e: Exception) {
            println("❌ Error during sigungu crawl: ${e.message}")
        } finally {
            driver.quit()
        }
    }

    // 읍면동 목록을 크롤링하여 CSV 파일로 저장하는 메서드
    fun crawlUmdList(outputFilePath: String = "csv/umd_list.csv") {
        val driver = webCrawler.createWebDriver() // Selenium WebDriver 생성
        val result = mutableListOf<Map<String, String>>() // 결과를 저장할 리스트

        try {
            // 법정동 코드 페이지 열기
            val url = "https://www.code.go.kr/stdcode/regCodeL.do"
            driver.get(url)
            println("🌐 Opened 법정동코드 페이지")

            // 최대 10초간 셀렉트 박스 로딩 대기
            val wait = WebDriverWait(driver, Duration.ofSeconds(10))
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Type1")))

            // 시도 선택 박스에서 옵션 가져오기
            val sidoOptions = driver.findElements(By.cssSelector("select#Type1 > option"))

            var autoCode = 1 // 전체 umd에 대해 1부터 시작하는 자동 코드

            // 각 시도에 대해 반복
            for (sidoOption in sidoOptions) {
                val sidoCode = sidoOption.getAttribute("value").trim() // 시도 코드
                val sidoName = sidoOption.text.trim() // 시도 이름

                // 시도 코드가 비어있거나 "시/도"인 경우 건너뜀
                if (sidoCode == "*" || sidoCode.isBlank() || sidoName == "시/도") continue

                // 시도 선택 후 시군구 목록 로딩
                (driver as JavascriptExecutor).executeScript("""
                    document.getElementById("Type1").value = "$sidoCode";
                    document.getElementById("Type1").dispatchEvent(new Event("change"));
                """.trimIndent())
                Thread.sleep(1000)

                // 시군구 목록을 Jsoup으로 파싱
                val sggOptions = Jsoup.parse(driver.pageSource).select("select#Type2 > option")

                // 각 시군구에 대해 반복
                for (sggOption in sggOptions) {
                    val sggCode = sggOption.attr("value").trim() // 시군구 코드
                    val sggName = sggOption.text().trim() // 시군구 이름

                    // 시군구 코드가 비어있거나 "시/군/구"인 경우 건너뜀
                    if (sggCode == "*" || sggCode.isBlank() || sggName == "시/군/구") continue

                    // 시군구 선택 후 읍면동 목록 로딩
                    println("🏙️ Processing SGG [$sggCode] $sggName")

                    // 자바스크립트를 사용하여 시군구 선택
                    (driver as JavascriptExecutor).executeScript("""
                        document.getElementById("Type2").value = "$sggCode";
                        document.getElementById("Type2").dispatchEvent(new Event("change"));
                    """.trimIndent())
                    Thread.sleep(1000)

                    // 읍면동 목록을 Jsoup으로 파싱
                    val doc = Jsoup.parse(driver.pageSource)
                    // 읍면동 선택 박스에서 옵션 가져오기
                    val umdOptions = doc.select("select#Type3 > option")

                    // 각 읍면동에 대해 반복
                    for (umdOption in umdOptions) {
                        val umdCodeRaw = umdOption.attr("value").trim() // 읍면동 코드 (사용 안함)
                        val umdName = umdOption.text().trim() // 읍면동 이름

                        // 읍면동 코드가 비어있거나 "*"인 경우 건너뜀
                        if (umdCodeRaw == "*" || umdCodeRaw.isBlank() || umdName == "읍/면/동") continue

                        val type = when {
                            umdName.endsWith("읍") -> "읍" // 읍면동 이름이 "읍"으로 끝나면 읍
                            umdName.endsWith("면") -> "면" // 읍면동 이름이 "면"으로 끝나면 면
                            umdName.endsWith("동") -> "동" // 읍면동 이름이 "동"으로 끝나면 동
                            else -> "기타" // 그 외의 경우 기타
                        }

                        val code = autoCode.toString().padStart(4, '0') // 전체 1부터 4자리

                        // 읍면동 정보 출력
                        println("    🏡 [$code] $umdName ($type)")

                        result.add(
                            mapOf(
                                "parent_name" to sggName, // 부모 시군구 이름
                                "code" to code, // 자동 생성된 읍면동 코드
                                "name" to umdName, // 읍면동 이름
                                "type" to type // 읍면동 타입 (읍, 면, 동, 기타
                            )
                        )

                        autoCode++ // 다음 읍면동을 위해 자동 코드 증가
                    }
                }
            }

            // 결과를 CSV 파일로 저장
            fileManager.writeCsv(outputFilePath, result, charset = Charset.forName("UTF-8"))
            println("✅ UMD list saved to $outputFilePath")

        } catch (e: Exception) {
            println("❌ Error during UMD crawl: ${e.message}")
        } finally {
            driver.quit()
        }
    }
}
