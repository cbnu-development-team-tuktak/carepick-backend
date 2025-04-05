package com.callrapport.controller.selfdiagnosis

// Service 관련
import com.callrapport.service.selfdiagnosis.SelfDiagnosisService // 자가진단 로직을 처리하는 서비스
import com.callrapport.service.selfdiagnosis.DiagnosisResult // 자가진단 결과를 담는 데이터 클래스

// Spring Web 관련 import
import org.springframework.web.bind.annotation.* // REST 컨트롤러 및 요청 매핑 어노테이션
import org.springframework.http.ResponseEntity // HTTP 응답 본문과 상태 코드를 담는 객체

@RestController
@RequestMapping("/api/self-diagnosis")
class SelfDiagnosisController(
    // 자가진단 비즈니스 로직을 처리
    private val selfDiagnosisService: SelfDiagnosisService
) {
    // 자가진단 키워드 기반 진단 결과 반환
    // 예: http://localhost:8080/api/self-diagnosis/keywords
    // POST 매핑으로 URL로는 접근 불가함. 직접 데이터 전송하도록 코드 구현 바람
    @PostMapping("/keywords")
    fun handleKeywords(@RequestBody symptoms: List<String>): ResponseEntity<DiagnosisResult> {
        // 서비스 계층을 통해 진단 결과 생성
        val response = selfDiagnosisService.generateResponse(symptoms)
        // 생성된 진단 결과를 HTTP 200 OK와 함께 반환
        return ResponseEntity.ok(response)
    }
}
