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

    // 증상 기반 자가진단 결과 반환
    // 예: http://localhost:8080/api/self-diagnosis/symptoms
    // POST 방식으로 증상 리스트(List<String>)를 전달해야 함
    @PostMapping("/symptoms")
    fun handleSymptoms(@RequestBody symptoms: List<String>): ResponseEntity<DiagnosisResult> {
        // 서비스 계층을 통해 진단 결과 생성
        val response = selfDiagnosisService.diagnoseBySymptoms(symptoms)
        // 생성된 진단 결과를 HTTP 200 OK와 함께 반환
        return ResponseEntity.ok(response)
    }

    // 질병명 기반 자가진단 결과 반환 (진료과 안내)
    // 예: http://localhost:8080/api/self-diagnosis/disease
    // POST 방식으로 질병명을 문자열 리스트(List<String>)로 전달해야 함
    @PostMapping("/disease")
    fun handleDisease(@RequestBody diseaseNames: List<String>): ResponseEntity<DiagnosisResult> {
        val response = selfDiagnosisService.diagnoseByDiseaseName(diseaseNames)
        return ResponseEntity.ok(response)
    }


    // 자연어 기반 자가진단 결과 반환 (향후 구현 예정)
    // 예: http://localhost:8080/api/self-diagnosis/natural
    // POST 방식으로 사용자 입력 문장을 전달해야 함
    @PostMapping("/natural")
    fun handleNaturalLanguage(@RequestBody inputText: String): ResponseEntity<DiagnosisResult> {
        // 서비스 계층을 통해 진단 결과 생성
        val response = selfDiagnosisService.diagnoseByNaturalLanguage(inputText)
        // 생성된 진단 결과를 HTTP 200 OK와 함께 반환
        return ResponseEntity.ok(response)
    }
}
