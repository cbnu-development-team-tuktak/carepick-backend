package com.callrapport.controller.test

import com.callrapport.component.crawler.HospitalCrawler
import com.callrapport.service.HospitalService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam


@RestController
@RequestMapping("/api/test")
class TestController(
    private val hospitalCrawler: HospitalCrawler, 
    private val hospitalService: HospitalService
) {
    // Query Parameter로 주소 받기 (`?address=서울 강남구 논현로 839`)
    @GetMapping("/coordinate")
    fun testGetCoordinates(@RequestParam address: String): Map<String, Any> {
        val coordinates = hospitalService.getCoordinatesFromAddress(address)

        return if (coordinates != null) {
            val (latitude, longitude) = coordinates
            val point = hospitalService.createPoint(latitude, longitude)

            mapOf(
                "address" to address,
                "latitude" to latitude, 
                "longitude" to longitude,
                "point" to point.toText()
            )
        } else {
            mapOf("error" to "좌표를 가져올 수 없습니다.", "address" to address)
        }
    }
}
