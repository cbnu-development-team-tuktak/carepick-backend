package com.callrapport.controller.disease

// Model 관련 import
import com.callrapport.model.disease.BodySystem // 신체계통(BodySystem) 엔티티

// Repository 관련 import
import com.callrapport.repository.disease.BodySystemRepository // BodySystem 엔티티를 다루는 리포지토리

// Spring Web 관련 import
import org.springframework.web.bind.annotation.GetMapping // GET 요청 매핑 어노테이션
import org.springframework.web.bind.annotation.RequestMapping // 컨트롤러의 기본 URL 설정
import org.springframework.web.bind.annotation.RestController // REST API 컨트롤러 명시

@RestController
@RequestMapping("/api/body-systems")
class BodySystemEntityController(
    private val bodySystemRepository: BodySystemRepository // 신체계통 데이터를 처리할 리포지토리 의존성 주입
) {
    // 전체 신체계통 목록 조회
    // 예: http://localhost:8080/api/body-systems
    @GetMapping
    fun getAllBodySystems(): List<BodySystem> {
        return bodySystemRepository.findAll() // 모든 신체계통 엔티티 조회
    }
}
