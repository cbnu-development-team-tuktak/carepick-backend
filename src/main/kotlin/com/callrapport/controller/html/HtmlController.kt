package com.callrapport.controller.html

import com.callrapport.utils.html.HtmlAnalyzer
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/html")
class HtmlController(
    private val htmlAnalyzer: HtmlAnalyzer
) {
    @GetMapping("/raw")
    fun getRawHtml(
        @RequestParam url: String, 
    ): ResponseEntity<String> {
        return try {
            val htmlContent = htmlAnalyzer.fetchHtml(url)

            // # Cotent-Type을 text/html로 명시하여 렌더링되지 않도록 설정
            val headers = HttpHeaders()
            headers.add(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
            ResponseEntity(htmlContent, headers, HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity.status(500).body("오류: ${e.message}")
        }
    }
    @GetMapping("/filter-by-text")
    fun getHtmlByTextContent(
        @RequestParam url: String,
        @RequestParam textContent: String
    ): ResponseEntity<String> {
        return try {
            val htmlContent = htmlAnalyzer.fetchHtmlWithTextContent(url, textContent)

            // # Cotent-Type을 text/html로 명시하여 렌더링되지 않도록 설정
            val headers = HttpHeaders()
            headers.add(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
            ResponseEntity(htmlContent, headers, HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity.status(500).body("오류: ${e.message}")
        }
    }
}