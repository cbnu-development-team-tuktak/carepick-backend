package com.callrapport.controller.crawl

import com.callrapport.service.crawler.CrawlerService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/crawl")
class CrawlController(
    private val crawlerService: CrawlerService
) {
    @GetMapping("/disease")
    fun initDisease(): ResponseEntity<List<String>> {
        return try {
            val diseaseLinks = crawlerService.fetchDiseaseLinks()
            ResponseEntity(diseaseLinks, HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(listOf("⚠️ 오류: ${e.message}"))
        }
    }

    @GetMapping("/hospital")
    fun initHospital(): ResponseEntity<String> {
        return try {
            crawlerService.fetchHospitalData()
            ResponseEntity("병원 데이터 크롤링 및 초기화 완료", HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("⚠️ 오류: ${e.message}")
        }
    }

    @GetMapping("/doctor")
    fun initDoctor(): ResponseEntity<String> {
        return try {
            crawlerService.fetchDoctorData()
            ResponseEntity("의사 데이터 크롤링 및 초기화 완료", HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("⚠️ 오류: ${e.message}")
        }
    }
}
