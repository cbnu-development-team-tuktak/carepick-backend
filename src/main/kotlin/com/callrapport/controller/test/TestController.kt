package com.callrapport.controller.test

// 크롤러 관련 import
import com.callrapport.component.crawler.ImageCrawler // 병원 이미지를 크롤링하는 클래스
import org.springframework.http.ResponseEntity // HTTP 응답을 생성하는 클래스
import org.springframework.web.bind.annotation.GetMapping // GET 요청을 처리하는 어노테이션
import org.springframework.web.bind.annotation.RequestMapping // 컨트롤러의 기본 URL을 설정하는 어노테이션
import org.springframework.web.bind.annotation.RequestParam // URL 요청 파라미터를 받는 어노테이션
import org.springframework.web.bind.annotation.RestController // REST 컨트롤러로 동작하도록 설정하는 어노테이션

@RestController
@RequestMapping("/api/test")
class TestController(
    private val imageCrawler: ImageCrawler
) {
    // 병원명을 받아서 네이버 지도에서 병원 이미지 크롤링
    @GetMapping("/image/hospital")
    fun getHospitalImages(@RequestParam hospitalName: String): ResponseEntity<Map<String, Any>> {
        val imageUrls: List<String> = imageCrawler.testNavigation(hospitalName) // 이미지 크롤링 실행

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
}
