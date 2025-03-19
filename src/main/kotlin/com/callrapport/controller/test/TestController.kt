package com.callrapport.controller.test

// 크롤러 관련 import
import com.callrapport.component.crawler.hospital.HospitalImageCrawler // 병원 이미지를 크롤링하는 클래스

// geolocation 관련 import
import com.callrapport.component.map.Geolocation // geolocation 클래스

// Spring 관련 import 
import org.springframework.http.ResponseEntity // HTTP 응답을 생성하는 클래스
import org.springframework.web.bind.annotation.GetMapping // GET 요청을 처리하는 어노테이션
import org.springframework.web.bind.annotation.RequestMapping // 컨트롤러의 기본 URL을 설정하는 어노테이션
import org.springframework.web.bind.annotation.RequestParam // URL 요청 파라미터를 받는 어노테이션
import org.springframework.web.bind.annotation.RestController // REST 컨트롤러로 동작하도록 설정하는 어노테이션

// Reactor 관련 import
import reactor.core.publisher.Mono // 비동기 단일 데이터 처리

@RestController
@RequestMapping("/api/test")
class TestController(
    private val hospitalImageCrawler: HospitalImageCrawler,
    private val geolocation: Geolocation
) {
    // 병원명을 받아서 네이버 지도에서 병원 이미지 크롤링
    @GetMapping("/image/hospital")
    fun getHospitalImages(@RequestParam hospitalName: String): ResponseEntity<Map<String, Any>> {
        val imageUrls: List<String> = hospitalImageCrawler.testNavigation(hospitalName) // 이미지 크롤링 실행

        return if (imageUrls.isNotEmpty()) {
            ResponseEntity.ok( // 성공 응답 반환
                mapOf(
                    "hospitalName" to hospitalName, // 요청한 병원명 포함
                    "status" to "Navigation successful.", // 성공 메시지
                    "imageUrls" to imageUrls // 병원 이미지 URL 리스트 포함
                )
            )
        } else {
            ResponseEntity.badRequest().body( // 실패 응답 반환
                mapOf(
                    "error" to "Hospital images not found.", // 오류 메시지
                    "hospitalName" to hospitalName // 요청한 병원명 포함
                )
            )
        }
    }

    // 주소로 Geocode 정보를 가져오는 메서드 (파라미터명을 hospitalName → address로 수정)
    @GetMapping("/geocode/hospital")
    fun getHospitalGeocode(@RequestParam address: String): Mono<ResponseEntity<String>> {
        return geolocation.getGeocode(address) // Geolocation 컴포넌트를 사용하여 네이버 API로부터 좌표 정보 조회 
            .map { geocodeResult ->
                // API에서 정상적으로 좌표 정보를 가져온 경우, 성공 응답(200 OK)와 함께 Geocode JSON 결과를 반환
                ResponseEntity.ok(geocodeResult)
            }
            // API에서 좌표 정보를 찾지 못한 경우, 실패 응답(400 Bad Request)와 함께 에러 메시지를 반환
            .defaultIfEmpty(ResponseEntity.badRequest().body("cannot get geocode info"))
    }
}