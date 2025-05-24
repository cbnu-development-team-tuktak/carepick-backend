package com.callrapport.service.crawl

// 크롤링 및 파일 관련 컴포넌트 import
import com.callrapport.component.crawler.WebCrawler // 웹 페이지에서 데이터 크롤링을 수행하는 컴포넌트
import com.callrapport.component.file.FileManager // 파일 저장 및 읽기 처리를 담당하는 유틸리티 클래스

// 대학 순위 및 지역 관련 모델 import
import com.callrapport.model.university.UniversityRank // 대학 순위 정보를 담는 엔티티
import com.callrapport.model.university.UniversityRankRegion // 대학과 지역 간의 매핑 정보를 담는 엔티티
import com.callrapport.model.university.Region // 대학이 속한 지역 정보를 담는 엔티티

// 대학 관련 리포지토리 import
import com.callrapport.repository.university.UniversityRankRepository // 대학 순위 엔티티에 대한 리포지토리
import com.callrapport.repository.university.UniversityRankRegionRepository // 대학 순위-지역 관계 엔티티에 대한 리포지토리
import com.callrapport.repository.university.RegionRepository // 지역 엔티티에 대한 리포지토리

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
class UniversityRankCrawlService(
    private val webCrawler: WebCrawler, // WebCrawler 의존성 주입
    private val fileManager: FileManager, // FileManager 의존성 주입
    private val universityRankRepository: UniversityRankRepository, // UniversityRankRepository 의존성 주입
    private val universityRankRegionRepository: UniversityRankRegionRepository, // UniversityRankRegionRepository 의존성 주입
    private val regionRepository: RegionRepository // RegionRepository 의존성 주입
) {

    fun crawlUniversityRanks(): List<Map<String, String>> {
        val driver = webCrawler.createWebDriver() // Selenium을 사용한 WebDriver 생성
        val result = mutableListOf<Map<String, String>>() // 결과를 저장할 리스트

        try {
            val baseUrl = "https://www.timeshighereducation.com/world-university-rankings/2025/subject-ranking/clinical-pre-clinical-health"
            driver.get(baseUrl) // THE 대학 랭킹 페이지 접속
            println("🌐 Opened THE ranking page")  // 페이지 접속 로그 출력

            // 최대 10초까지 로딩 대기
            val wait = WebDriverWait(driver, Duration.ofSeconds(10))

            while (true) {
                // 테이블 로딩 대기
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table#datatable-1 > tbody > tr")))
                Thread.sleep(2000) // 안정성 확보를 위한 대기

                val doc = Jsoup.parse(driver.pageSource) // 페이지 소스를 Jsoup으로 파싱
                val rows = doc.select("table#datatable-1 > tbody > tr") // 대학 랭킹 테이블의 각 행 출력
                println("📦 Found ${rows.size} universities on current page") // 파싱된 대학 수 출력

                for (row in rows) {
                    try {
                        // 대학 순위 추출
                        val rank = row.selectFirst("td.rank")?.text()?.trim() ?: continue 

                        // 대학 이름 태그 추출
                        val nameTag = row.selectFirst("td.name a.ranking-institution-title") ?: continue

                        // 대학 이름 텍스트 추출
                        val name = nameTag.text().trim()

                        // 지역 정보 추출
                        val region = row.selectFirst("td.name .location span a")?.text()?.trim() ?: "Unknown"
                        
                        // 순위, 대학명, 지역 정보 로그 출력
                        println("🏫 [$rank] $name | $region")

                        result.add(
                            mapOf(
                                "rank" to rank, // 대학 순위 저장
                                "name" to name, // 대학명 저장
                                "region" to region // 지역 정보 저장
                            )
                        )
                    } catch (e: Exception) { // 파싱을 실패한 경우
                        // 오류 메시지 출력
                        println("⚠️ Failed to parse a university row: ${e.message}") 
                    }
                }
                
                // 다음 페이지 버튼 탐색
                val nextButton = driver.findElements(By.id("datatable-1_next")).firstOrNull()

                // 버튼 비활성화 여부 확인
                val isDisabled = nextButton?.getAttribute("class")?.contains("disabled") ?: true

                if (isDisabled) { // 페이지네이션 버튼이 더 이상 없는 경우
                    println("✅ Reached last page. Stopping pagination.") // 마지막 페이지 도달 안내 로그 출력
                    break // 반복문 종료
                } else {
                    println("➡️ Moving to next page...") // 페이지 이동 안내 로그 출력
                    (driver as JavascriptExecutor).executeScript("arguments[0].click();", nextButton) // 자바스크립트로 클릭 실행 
                    Thread.sleep(3000) // 페이지 전환 대기
                }
            }
        } catch (e: Exception) { // 크롤링 중 오류가 발생한 경우 
            // 에러 발생 로그 출력
            println("❌ Error during crawl: ${e.message}") 
        } finally { // 크롤링 마무리할 경우
            driver.quit() // WebDriver 자원 해제
        }

        return result // 수집된 대학 정보 리스트 반환
    }

    fun mergeWithTranslation(
        // 원본 CSV 파일 경로
        originalFilePath: String = "csv/university_rankings.csv",

        // 번역된 CSV 파일 경로
        translatedFilePath: String = "csv/university_rankings_translated.csv",

        // 병합 후 저장할 CSV 파일 경로
        outputFilePath: String = "csv/university_rankings_final.csv"
    ) {
        // 왼본 CSV 파일 읽기
        val originalData = fileManager.readCsv(originalFilePath, charset = Charset.forName("UTF-8"))
        
        // 번역된 CSV 파일 읽기
        val translatedData = fileManager.readCsv(translatedFilePath, charset = Charset.forName("UTF-8"))

        // 원본 CSV 파일과 번역된 CSV 파일의 행 수가 다를 경우
        if (originalData.size != translatedData.size) {
            // 경로 메시지 로그 출력
            println("⚠️ Data size mismatch: original=${originalData.size}, translated=${translatedData.size}")
        }

        // 원본 데이터와 번역 데이터를 인덱스 기준으로 병합
        val merged = originalData.zip(translatedData).mapIndexed { index, (original: Map<String, String>, translated: Map<String, String>) ->
            mapOf(
                "rank" to (index + 1).toString(), // 순위는 인덱스 + 1로 설정
                "kr_name" to translated["name"]?.trim().orEmpty(), // 대학명 (한국어)
                "en_name" to original["name"]?.trim().orEmpty(), // 대학명 (영어)
                "region" to translated["region"]?.trim().orEmpty() // 지역명
            )
        }

        // 병합된 데이터를 저장된 경로에 CSV 형식으로 저장 (UTF-8 인코딩 사용)
        fileManager.writeCsv(outputFilePath, merged, charset = Charset.forName("UTF-8"))

        // 저장 완료 로그 출력
        println("✅ Merged CSV saved successfully with row-number-based rank: $outputFilePath")
    }

    // 통합한 의대 순위 csv를 DB에 저장
    fun saveUniversityRanks(
        // 저장된 의대 순위 CSV 파일 경로
        filePath: String = "csv/university_rankings_final.csv"
    ) {
        // 지정한 CSV 파일을 UTF-8 인코딩으로 불러옴
        val data = fileManager.readCsv(filePath, charset = Charset.forName("UTF-8"))

        // 이미 DB에 저장된 한글 대학명 목록을 Set로 저장 (중복 확인용)
        val existingKoreanNames = universityRankRepository.findAll().map { it.krName }.toSet()
        // 이미 DB에 저장된 영문 대학명 목록을 Set로 저장 (중복 확인용)
        val existingEnglishNames = universityRankRepository.findAll().map { it.enName }.toSet()

        // 처리 중 중복을 방지하기 위한 한글 대학명 목록
        val seenKr = mutableSetOf<String>()
        // 처리 중 중복을 방지하기 위한 영문 대학명 목록
        val seenEn = mutableSetOf<String>()

        // 지역 이름을 키로 하여 Region 엔티티를 맵으로 변환 
        val regionMap = regionRepository.findAll().associateBy { it.name }.toMutableMap()

        // 새로 저장할 의대 순위 엔티티 목록
        val newUniversityRanks = mutableListOf<UniversityRank>()
        // 새로 저장할 의대 순위-지역 관계 엔티티 목록
        val newUniversityRankRegions = mutableListOf<UniversityRankRegion>()

        data.forEach { row ->
            // 순위 값을 정수로 변환
            val rank = row["rank"]?.toIntOrNull() ?: return@forEach
            // 한글 대학명 추출 및 공백 제거 
            val kr = row["kr_name"]?.trim() ?: return@forEach
            // 영문 대학명 추출 및 공백 제거
            val en = row["en_name"]?.trim() ?: return@forEach
            // 지역명 추출 및 공백 제거 
            val regionName = row["region"]?.trim() ?: return@forEach

            // 이미 존재하거나 중복되는 한글 대학명이라면 건너뜀
            if (kr in existingKoreanNames || kr in seenKr) return@forEach
            // 이미 존재하거나 중복되는 영문 대학명이라면 건너뜀
            if (en in existingEnglishNames || en in seenEn) return@forEach
            
            seenKr.add(kr) // 중복 방지를 위해 한글 대학명을 set에 추가
            seenEn.add(en) // 중복 방지를 위해 영문 대학명을 set에 추가

            // 지역명이 regionMap에 없는 경우 새 Region 엔티티를 저장하고 추가
            // 지역명이 regionMap에 있는 경우 기존 값을 사용
            val region = regionMap.getOrPut(regionName) {
                val newRegion = regionRepository.save(Region(name = regionName))
                newRegion
            }

            // 의대 랭킹 엔티티 생성
            val university = UniversityRank(
                id = rank, // rank를 id로 사용
                krName = kr, // 한글 대학명
                enName = en // 영문 대학명
            )
            // 생성한 대학 랭킹 엔티티를 저장 목록에 추가
            newUniversityRanks.add(university)

            // 의대 순위-지역 관계 엔티티 생성 (연결)
            val mapping = UniversityRankRegion(
                universityRank = university, // 의대 순위 엔티티
                region = region // 지역 엔티티
            )
            // 생성한 매핑 엔티티를 저장 목록에 추가
            newUniversityRankRegions.add(mapping)
        }

        // 생성한 대학 랭킹 엔티티를 DB에 일괄 저장
        universityRankRepository.saveAll(newUniversityRanks)
        // 생성된 대학-지역 매핑 엔티티들을 DB에 일괄 저장
        universityRankRegionRepository.saveAll(newUniversityRankRegions)

        // 저장 완료를 보여주기 위한 대학 랭킹 수 로그 출력
        println("✅ ${newUniversityRanks.size} universities saved.")
        // 저장 완료를 보여주기 위한 대학 랭킹-지역 매핑 수 로그 출력
        println("✅ ${newUniversityRankRegions.size} university-region mappings saved.")
    }
}
