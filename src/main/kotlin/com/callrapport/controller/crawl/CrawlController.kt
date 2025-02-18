package com.callrapport.controller.crawl

// 크롤러 관련 의존성
import com.callrapport.component.crawler.WebCrawler
import com.callrapport.component.crawler.DiseaseCrawler

// Spring 관련 라이브러리
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/crawl")
class CrawlController(
    private val webCrawler: WebCrawler, 
    private val diseaseCrawler: DiseaseCrawler 
) {
    // 전체 HTML 반환
    @GetMapping("/raw")
    fun getRawHtml(
        @RequestParam url: String
    ): ResponseEntity<String> {
        return try {
            val htmlContent = webCrawler.fetchHtml(url)

            // # Content-Type을 text/html로 명시하여 렌더링되지 않도록 설정
            val headers = HttpHeaders()
            headers.add(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
            ResponseEntity(htmlContent, headers, HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity.status(500).body("오류: ${e.message}")
        }
    }

    // 특정 텍스트를 포함한 HTML 반환
    @GetMapping("/filter-by-text")
    fun getHtmlByTextContent(
        @RequestParam url: String,
        @RequestParam textContent: String
    ): ResponseEntity<String> {
        return try {
            val htmlContent = webCrawler.fetchHtmlWithTextContent(url, textContent)

            // # Content-Type을 text/html로 명시하여 렌더링되지 않도록 설정
            val headers = HttpHeaders()
            headers.add(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
            ResponseEntity(htmlContent, headers, HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity.status(500).body("⚠️ error occured: ${e.message}")
        }
    }

    // 질병 정보를 크롤링하여 링크 반환
    @GetMapping("/disease")
    fun initDisease(): ResponseEntity<List<String>> {
        return try {
            val diseaseLinks = diseaseCrawler.getDiseaseLinks()
            ResponseEntity(diseaseLinks, HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(listOf("⚠️ error occured: ${e.message}"))
        }
    }
}
