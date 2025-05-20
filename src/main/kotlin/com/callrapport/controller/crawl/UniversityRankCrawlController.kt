package com.callrapport.controller.crawl

import com.callrapport.component.file.FileManager
import com.callrapport.service.UniversityRankCrawlService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/crawl/university-rank")
class UniversityRankCrawlController(
    private val universityRankCrawlService: UniversityRankCrawlService,
    private val fileManager: FileManager
) {

    @GetMapping
    fun crawlAndSaveCSV(): ResponseEntity<String> {
        return try {
            val data = universityRankCrawlService.crawlUniversityRanks()
            if (data.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body("⚠️ No data found on THE ranking page.")
            }

            val filePath = "csv/university_rankings.csv"
            fileManager.writeCsv(filePath, data)

            ResponseEntity.ok("✅ University rankings saved to CSV: $filePath")
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("❌ Failed to crawl and save university rankings: ${e.message}")
        }
    }

    @GetMapping("/merge")
    fun mergeTranslatedCSV(): ResponseEntity<String> {
        return try {
            universityRankCrawlService.mergeWithTranslation()
            ResponseEntity.ok("✅ Merged CSV created at: csv/university_rankings_final.csv")
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("❌ Failed to merge CSVs: ${e.message}")
        }
    }
}
