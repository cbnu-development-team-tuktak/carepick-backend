package com.callrapport.service.crawl

import com.callrapport.component.crawler.WebCrawler
import com.callrapport.component.file.FileManager

import com.callrapport.model.university.UniversityRank
import com.callrapport.model.university.UniversityRankRegion
import com.callrapport.model.university.Region

import com.callrapport.repository.university.UniversityRankRepository
import com.callrapport.repository.university.UniversityRankRegionRepository
import com.callrapport.repository.university.RegionRepository

import org.jsoup.Jsoup
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.stereotype.Service
import java.nio.charset.Charset
import java.time.Duration

@Service
class UniversityRankCrawlService(
    private val webCrawler: WebCrawler,
    private val fileManager: FileManager,
    private val universityRankRepository: UniversityRankRepository,
    private val universityRankRegionRepository: UniversityRankRegionRepository,
    private val regionRepository: RegionRepository
) {

    fun crawlUniversityRanks(): List<Map<String, String>> {
        val driver = webCrawler.createWebDriver()
        val result = mutableListOf<Map<String, String>>()

        try {
            val baseUrl = "https://www.timeshighereducation.com/world-university-rankings/2025/subject-ranking/clinical-pre-clinical-health"
            driver.get(baseUrl)
            println("🌐 Opened THE ranking page")

            val wait = WebDriverWait(driver, Duration.ofSeconds(10))

            while (true) {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table#datatable-1 > tbody > tr")))
                Thread.sleep(2000)

                val doc = Jsoup.parse(driver.pageSource)
                val rows = doc.select("table#datatable-1 > tbody > tr")
                println("📦 Found ${rows.size} universities on current page")

                for (row in rows) {
                    try {
                        val rank = row.selectFirst("td.rank")?.text()?.trim() ?: continue
                        val nameTag = row.selectFirst("td.name a.ranking-institution-title") ?: continue
                        val name = nameTag.text().trim()
                        val region = row.selectFirst("td.name .location span a")?.text()?.trim() ?: "Unknown"

                        println("🏫 [$rank] $name | $region")

                        result.add(
                            mapOf(
                                "rank" to rank,
                                "name" to name,
                                "region" to region
                            )
                        )
                    } catch (e: Exception) {
                        println("⚠️ Failed to parse a university row: ${e.message}")
                    }
                }

                val nextButton = driver.findElements(By.id("datatable-1_next")).firstOrNull()
                val isDisabled = nextButton?.getAttribute("class")?.contains("disabled") ?: true

                if (isDisabled) {
                    println("✅ Reached last page. Stopping pagination.")
                    break
                } else {
                    println("➡️ Moving to next page...")
                    (driver as JavascriptExecutor).executeScript("arguments[0].click();", nextButton)
                    Thread.sleep(3000)
                }
            }

        } catch (e: Exception) {
            println("❌ Error during crawl: ${e.message}")
        } finally {
            driver.quit()
        }

        return result
    }

    fun mergeWithTranslation(
        originalFilePath: String = "csv/university_rankings.csv",
        translatedFilePath: String = "csv/university_rankings_translated.csv",
        outputFilePath: String = "csv/university_rankings_final.csv"
    ) {
        val originalData = fileManager.readCsv(originalFilePath, charset = Charset.forName("UTF-8"))
        val translatedData = fileManager.readCsv(translatedFilePath, charset = Charset.forName("UTF-8"))

        if (originalData.size != translatedData.size) {
            println("⚠️ Data size mismatch: original=${originalData.size}, translated=${translatedData.size}")
        }

        val merged = originalData.zip(translatedData).mapIndexed { index, (original: Map<String, String>, translated: Map<String, String>) ->
            mapOf(
                "rank" to (index + 1).toString(),
                "kr_name" to translated["name"]?.trim().orEmpty(),
                "en_name" to original["name"]?.trim().orEmpty(),
                "region" to translated["region"]?.trim().orEmpty()
            )
        }

        fileManager.writeCsv(outputFilePath, merged, charset = Charset.forName("UTF-8"))
        println("✅ Merged CSV saved successfully with row-number-based rank: $outputFilePath")
    }

    fun saveUniversityRanks(
        filePath: String = "csv/university_rankings_final.csv"
    ) {
        val data = fileManager.readCsv(filePath, charset = Charset.forName("UTF-8"))

        // 기존 대학명 체크용 세트
        val existingKoreanNames = universityRankRepository.findAll().map { it.krName }.toSet()
        val existingEnglishNames = universityRankRepository.findAll().map { it.enName }.toSet()

        val seenKr = mutableSetOf<String>()
        val seenEn = mutableSetOf<String>()

        // 지역 캐시 (중복 Region 생성을 방지)
        val regionMap = regionRepository.findAll().associateBy { it.name }.toMutableMap()

        val newUniversityRanks = mutableListOf<UniversityRank>()
        val newUniversityRankRegions = mutableListOf<UniversityRankRegion>()

        data.forEach { row ->
            val rank = row["rank"]?.toIntOrNull() ?: return@forEach
            val kr = row["kr_name"]?.trim() ?: return@forEach
            val en = row["en_name"]?.trim() ?: return@forEach
            val regionName = row["region"]?.trim() ?: return@forEach

            if (kr in existingKoreanNames || kr in seenKr) return@forEach
            if (en in existingEnglishNames || en in seenEn) return@forEach

            seenKr.add(kr)
            seenEn.add(en)

            // Region 찾거나 새로 생성
            val region = regionMap.getOrPut(regionName) {
                val newRegion = regionRepository.save(Region(name = regionName))
                newRegion
            }

            // UniversityRank 생성
            val university = UniversityRank(
                id = rank,
                krName = kr,
                enName = en
            )
            newUniversityRanks.add(university)

            // 중간 테이블 생성
            val mapping = UniversityRankRegion(
                universityRank = university,
                region = region
            )
            newUniversityRankRegions.add(mapping)
        }

        // 저장 순서 중요: 대학 먼저, 중간 테이블 다음
        universityRankRepository.saveAll(newUniversityRanks)
        universityRankRegionRepository.saveAll(newUniversityRankRegions)

        println("✅ ${newUniversityRanks.size} universities saved.")
        println("✅ ${newUniversityRankRegions.size} university-region mappings saved.")
    }
}
