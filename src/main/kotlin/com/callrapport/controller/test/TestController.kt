package com.callrapport.controller.test

import com.callrapport.component.crawler.hospital.HospitalImageCrawler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test")
class TestController(
    private val hospitalImageCrawler: HospitalImageCrawler
) {
    /**
     * ✅ 병원명을 받아서 네이버 이미지 검색에서 이미지 URL을 반환하는 API
     */
    @GetMapping("/place/images")
    fun getHospitalImages(@RequestParam hospitalName: String): ResponseEntity<Map<String, Any>> {
        val imageSources = hospitalImageCrawler.crawlHospitalImages(hospitalName) // 네이버 이미지 검색 크롤링

        if (imageSources.isEmpty()) {
            return ResponseEntity.badRequest().body(
                mapOf(
                    "error" to "Failed to retrieve hospital images.",
                    "hospitalName" to hospitalName
                )
            )
        }

        return ResponseEntity.ok(
            mapOf(
                "hospitalName" to hospitalName,
                "status" to "Images extracted successfully.",
                "imageSources" to imageSources
            )
        )
    }
}
