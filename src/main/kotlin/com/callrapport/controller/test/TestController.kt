package com.callrapport.controller.test

// 크롤러 import
import com.callrapport.component.crawler.hospital.HospitalImageCrawler // 병원 이미지를 크롤링하는 컴포넌트

// Spring 관련 import
import org.springframework.http.ResponseEntity // HTTP 응답 객체
import org.springframework.web.bind.annotation.GetMapping // GET 요청 처리 어노테이션
import org.springframework.web.bind.annotation.RequestMapping // URL 매핑 어노테이션
import org.springframework.web.bind.annotation.RequestParam // 쿼리 파라미터 추출 어노테이션
import org.springframework.web.bind.annotation.RestController // REST 컨트롤러 선언

@RestController
@RequestMapping("/api/test")
class TestController(
    private val hospitalImageCrawler: HospitalImageCrawler // 병원 이미지 크롤러
) {
    // 병원명을 받아서 네이버 이미지 검색 결과에서 이미지 URL 리스트를 반환
    // ex) http://localhost:8080/api/test/place/images?hospitalName=베이드의원
    @GetMapping("/place/images")
    fun getHospitalImages(@RequestParam hospitalName: String): ResponseEntity<Map<String, Any>> {
        // 병원명을 이용해 네이버 이미지 검색 결과 크롤링 수행
        val imageSources = hospitalImageCrawler.crawlHospitalImages(hospitalName)
        
        // 이미지가 없는 경우 400 Bad Request 응답 반환
        if (imageSources.isEmpty()) {
            return ResponseEntity.badRequest().body(
                mapOf(
                    "error" to "Failed to retrieve hospital images.", // 에러 메시지
                    "hospitalName" to hospitalName // 요청에 사용된 병원 이름
                )
            )
        }

        // 크롤링 성공 시: 병원 이름과 이미지 URL 리스트를 응답으로 반환
        return ResponseEntity.ok(
            mapOf(
                "hospitalName" to hospitalName, // 요청된 병원 이름
                "status" to "Images extracted successfully.", // 처리 상태 메시지
                "imageSources" to imageSources // 크롤링된 이미지 URL 리스트
            )
        )
    }
}
