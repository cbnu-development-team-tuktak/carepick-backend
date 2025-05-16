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
    private val selfDiagnosisService: SelfDiagnosisService // 자가진단 비즈니스 로직을 처리
) {

    // 자연어 기반 자가진단 - mini 버전 (GET)
    // 예: http://localhost:8080/api/self-diagnosis/natural/mini?text=재채기가%20나와&k=3
    @GetMapping("/natural/mini")
    fun getNaturalMini(
        @RequestParam text: String, // 증상 설명 텍스트
        @RequestParam(required = false, defaultValue = "3") k: Int // Top-k 예측 개수
    ): ResponseEntity<DiagnosisResult> {
        // 서비스 호출하여 예측 수행
        val response = selfDiagnosisService.diagnoseNaturalMini(text, k)

        // 에측 결과를 200 OK 응답으로 반환
        return ResponseEntity.ok(response)
    }

    // 자연어 기반 자가진단 - mini 버전 (POST)
    // 예: POST http://localhost:8080/api/self-diagnosis/natural/mini
    @PostMapping("/natural/mini")
    fun postNaturalMini(
        @RequestBody text: String, // 증상 설명 텍스트
        @RequestParam(required = false, defaultValue = "3") k: Int // Top-k 예측 개수
    ): ResponseEntity<DiagnosisResult> {
        // 서비스 호출로 예측 수행
        val response = selfDiagnosisService.diagnoseNaturalMini(text, k) 

        // 예측 결과를 200 OK로 반환
        return ResponseEntity.ok(response) 
    }

    // 자연어 기반 자가진단 - advanced 버전 (GET)
    // 예: http://localhost:8080/api/self-diagnosis/natural/advanced?text=속이%20메스껍고%20열이%20납니다
    @GetMapping("/natural/advanced")
    fun getNaturalAdvanced(
        @RequestParam text: String // 증상 설명 텍스트
    ): ResponseEntity<DiagnosisResult> {
        // 서비스 호출로 예측 수행
        val response = selfDiagnosisService.diagnoseNaturalAdvanced(text)

        // 예측 결과를 200 OK로 반환
        return ResponseEntity.ok(response)
    }

    // 자연어 기반 자가진단 - advanced 버전 (POST)
    // 예: POST http://localhost:8080/api/self-diagnosis/natural/advanced
    @PostMapping("/natural/advanced")
    fun postNaturalAdvanced(
        @RequestBody text: String // 증상 설명 텍스트
    ): ResponseEntity<DiagnosisResult> {
        // 서비스 호출로 예측 수행
        val response = selfDiagnosisService.diagnoseNaturalAdvanced(text)

        // 예측 결과를 200 OK로 반환
        return ResponseEntity.ok(response)
    }
}
