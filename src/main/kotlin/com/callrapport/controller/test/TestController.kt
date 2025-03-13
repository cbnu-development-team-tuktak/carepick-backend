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

    // ✅ 새로운 엔드포인트 (location 저장 X)
    @GetMapping("/save-db-no-location")
    fun testSaveHospitalInfosToDBNoLocation(): ResponseEntity<String> {
        return try {
            hospitalService.deleteAllHospitalData() // 병원 관련 테이블 데이터 삭제
            val hospitalLinks = hospitalCrawler.crawlHospitalLinks()

            hospitalLinks.forEach { (name, url) ->
                val hospitalId = extractHospitalIdFromUrl(url)
                val hospitalInfo = hospitalCrawler.crawlHospitalInfos(name, url)

                val additionalInfo: Map<String, Any> = hospitalInfo["additional_info"]?.toString()?.let { info ->
                    try {
                        ObjectMapper().readValue(info, object : TypeReference<Map<String, Any>>() {})
                    } catch (e: Exception) {
                        emptyMap()
                    }
                } ?: emptyMap()

                val specialties = hospitalInfo["specialties"]?.toString()?.split(" | ") ?: emptyList()

                // ✅ location 없이 병원 정보 저장
                hospitalService.saveHospitalWithoutCoordinates(
                    id = hospitalId,
                    name = name,
                    phoneNumber = hospitalInfo["phone_number"]?.toString(),
                    homepage = hospitalInfo["homepage"]?.toString(),
                    address = hospitalInfo["address"]?.toString() ?: "",
                    operatingHours = hospitalInfo["operating_hours"]?.toString(),
                    specialties = specialties,
                    url = url,
                    additionalInfo = additionalInfo
                )                
            }

            ResponseEntity("All hospital saved successfully (without location)", HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity.status(500).body("⚠️ Error occurred while resetting and saving hospitals: ${e.message}")
        }
    }

    // URL에서 hospitalId 추출하는 함수
    fun extractHospitalIdFromUrl(url: String): String {
        return url.substringAfterLast("/")
    }
}
