package com.callrapport.controller.crawl

// 크롤러 관련 import
import com.callrapport.component.crawler.WebCrawler // WebCrawler: 웹페이지를 크롤링하는 기본 클래스
import com.callrapport.component.crawler.DiseaseCrawler // DiseaseCrawler: 질병 정보를 크롤링하는 클래스
import com.callrapport.component.crawler.HospitalCrawler // HospitalCrawler: 병원 정보를 크롤링하는 클래스
import com.callrapport.component.csv.CSVWriter // 크롤링한 데이터를 CSV 파일로 저장하는 유틸리티 클래스

// Spring 관련 import
import org.springframework.http.HttpHeaders // HTTP 요청/응답 헤더를 처리하는 클래스
import org.springframework.http.HttpStatus // HTTP 응답 상태 코드(200, 400, 500)를 정의하는 클래스
import org.springframework.http.ResponseEntity // HTTP 응답을 커스텀하기 위한 클래스 (응답 데이터 + 상태 코드 포함)
import org.springframework.web.bind.annotation.* 

import java.nio.file.Paths // 파일 경로를 다루는 유틸리티 클래스

@RestController
@RequestMapping("/api/crawl")
class CrawlController(
    private val webCrawler: WebCrawler, // 웹 크롤러 
    private val diseaseCrawler: DiseaseCrawler, // 질병 정보 크롤러
    private val hospitalCrawler: HospitalCrawler, // 병원 정보 크롤러
    private val csvWriter: CSVWriter, // CSV 저장 유틸리티
) { 
    // ✅ 질병 정보 크롤링 (병명, 링크 반환)
    @GetMapping("/disease/links")
    fun getDiseaseLinks(): ResponseEntity<List<Map<String, String>>> {
        return try {
            val diseaseLinks = diseaseCrawler.crawlDiseaseLinks()
            val response = diseaseLinks.map { (name, url) -> mapOf("name" to name, "url" to url) }
            ResponseEntity(response, HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(listOf(mapOf("error" to "⚠️ Error occurred: ${e.message}")))
        }
    }

    // ✅ 질병 정보 크롤링 (상세 정보 포함)
    @GetMapping("/disease/infos")
    fun getDiseaseInfos(): ResponseEntity<List<Map<String, String?>>> {
        return try {
            val diseaseLinks = diseaseCrawler.crawlDiseaseLinks()
            val diseaseInfos = diseaseLinks.map { (name, url) ->
                diseaseCrawler.crawlDiseaseInfos(name, url)
            }
            ResponseEntity(diseaseInfos, HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(listOf(mapOf("error" to "⚠️ Error occurred: ${e.message}")))
        }
    }

    // ✅ 크롤링된 질병 정보를 CSV로 저장하는 API
    @GetMapping("/disease/save-csv")
    fun saveDiseaseInfosToCSV(): ResponseEntity<String> {
        return try {
            val diseaseLinks = diseaseCrawler.crawlDiseaseLinks()
            val diseaseInfos = diseaseLinks.map { (name, url) ->
                diseaseCrawler.crawlDiseaseInfos(name, url)
            }

            val filePath = Paths.get("disease_info.csv").toAbsolutePath().toString()
            val success = csvWriter.writeToCSV(diseaseInfos, filePath)

            if (success) {
                ResponseEntity("✅ writing disease_info.csv successfully to $filePath", HttpStatus.OK)
            } else {
                ResponseEntity.status(500).body("⚠️ writing disease_info.csv failed")
            }
        } catch (e: Exception) {
            ResponseEntity.status(500).body("⚠️ Error occurred: ${e.message}")
        }
    }
}
