package com.callrapport.controller.crawl

// 크롤러 관련 import
import com.callrapport.component.crawler.WebCrawler // WebCrawler: 웹페이지를 크롤링하는 기본 클래스
import com.callrapport.component.crawler.DiseaseCrawler // DiseaseCrawler: 질병 정보를 크롤링하는 클래스

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
    private val diseaseCrawler: DiseaseCrawler // 질병 정보 크롤러
) { 
    // 질병 링크 목록 크롤링 (병명과 URL만 반환)
    @GetMapping("/disease/links")
    fun getDiseaseLinks(): ResponseEntity<List<Map<String, String>>> {
        return try {
            val diseaseLinks = diseaseCrawler.crawlDiseaseLinks() // 질병 이름과 링크 목록 수집 
            val response = diseaseLinks.map { (name, url) -> mapOf("name" to name, "url" to url) } // Map 형식으로 변환
            ResponseEntity(response, HttpStatus.OK) // 정상 응답 반환
        } catch (e: Exception) {
            // 에러 발생 시 에러 메시지를 포함한 500 응답 반환
            ResponseEntity.status(500).body(listOf(mapOf("error" to "⚠️ Error occurred: ${e.message}")))
        }
    }

    // 질병 상세 정보 크롤링
    @GetMapping("/disease/infos")
    fun getDiseaseInfos(): ResponseEntity<List<Map<String, String?>>> {
        return try {
            val diseaseLinks = diseaseCrawler.crawlDiseaseLinks() // 질병 링크 목록 수집 
            val diseaseInfos = diseaseLinks.map { (name, url) ->
                diseaseCrawler.crawlDiseaseInfos(name, url) // 각 링크에 대해 상세 정보 수집
            }
            ResponseEntity(diseaseInfos, HttpStatus.OK) // 정상 응답 반환
        } catch (e: Exception) {
            // 에러 발생 시 에러 메시지를 포함한 500 응답 반환
            ResponseEntity.status(500).body(listOf(mapOf("error" to "⚠️ Error occurred: ${e.message}")))
        }
    }
}
