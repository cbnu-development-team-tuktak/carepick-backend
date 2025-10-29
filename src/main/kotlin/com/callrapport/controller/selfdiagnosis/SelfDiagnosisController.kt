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
    // 자연어 기반 자가진단 - 질병 예측 (GET)
    // 예: GET http://localhost:8080/api/self-diagnosis/disease?text=알레르기가%20있어요&k=3
    @GetMapping("/disease")
    fun getDiseasePrediction(
        @RequestParam text: String, // 증상 설명 텍스트
        @RequestParam(required = false, defaultValue = "3") k: Int // Top-k 예측 가능
    ): ResponseEntity<DiagnosisResult> {
        // 서비스 호출로 예측 수행
        val response = selfDiagnosisService.diagnoseDisease(text, k)

        // 예측 결과를 200 OK로 반환
        return ResponseEntity.ok(response)
    }

    // 자연어 기반 자가진단 - 질병 예측 (POST)
    // 예: POST http://localhost:8080/api/self-diagnosis/disease
    @PostMapping("/disease")
    fun postDiseasePrediction(
        @RequestBody text: String, // 증상 설명 텍스트
        @RequestParam(required = false, defaultValue = "3") k: Int // Top-k 예측 개수
    ): ResponseEntity<DiagnosisResult> {
        // 서비스 호출로 예측 수행
        val response = selfDiagnosisService.diagnoseDisease(text, k)

        // 예측 결과를 200 OK로 반환
        return ResponseEntity.ok(response)
    }

    // 자연어 기반 자가진단 - 진료과 예측 (GET)
    // 예: GET http://localhost:8080/api/self-diagnosis/specialty?text=허리가%20아파요&k=3
    @GetMapping("/specialty")
    fun getSpecialtyPrediction(
        @RequestParam text: String, // 증상 설명 텍스트
        @RequestParam(required = false, defaultValue = "3") k: Int // Top-k 예측 개수
    ): ResponseEntity<DiagnosisResult> {
        // 서비스 호출로 예측 수행
        val response = selfDiagnosisService.diagnoseSpecialty(text, k)

        // 예측 결과를 200 OK로 반환
        return ResponseEntity.ok(response)
    }

    // 자연어 기반 자가진단 - 진료과 예측 (POST)
    // 예: POST http://localhost:8080/api/self-diagnosis/specialty
    @PostMapping("/specialty")
    fun postSpecialtyPrediction(
        @RequestBody text: String, // 증상 설명 텍스트
        @RequestParam(required = false, defaultValue = "3") k: Int // Top-k 예측 개수
    ): ResponseEntity<DiagnosisResult> {
        // 서비스 호출로 예측 수행
        val response = selfDiagnosisService.diagnoseSpecialty(text, k)

        // 예측 결과를 200 OK로 반환
        return ResponseEntity.ok(response)
    }

    // ChatGPT 질병 예측 테스트 (GET)
    // 예: GET http://localhost:8080/api/self-diagnosis/disease/gpt?text=몸이%20으슬으슬해요
    @GetMapping("/disease/gpt")
    fun getGptDiseasePrediction(@RequestParam text: String): ResponseEntity<DiagnosisResult> {
        val response = selfDiagnosisService.diagnoseDiseaseWithGpt(text)
        return ResponseEntity.ok(response)
    }

    // ChatGPT 질병 예측 테스트 (POST)
    // 예: POST http://localhost:8080/api/self-diagnosis/disease/gpt
    @PostMapping("/disease/gpt")
    fun postGptDiseasePrediction(@RequestBody text: String): ResponseEntity<DiagnosisResult> {
        val response = selfDiagnosisService.diagnoseDiseaseWithGpt(text)
        return ResponseEntity.ok(response)
    }

    // ChatGPT 진료과 예측 테스트 (GET)
    // 예: GET http://localhost:8080/api/self-diagnosis/specialty/gpt?text=몸이%20으슬으슬해요
    @GetMapping("/specialty/gpt")
    fun getGptSpecialtyPrediction(@RequestParam text: String): ResponseEntity<DiagnosisResult> {
        val response = selfDiagnosisService.diagnoseSpecialtyWithGpt(text)
        return ResponseEntity.ok(response)
    }

    // ChatGPT 진료과 예측 테스트 (POST)
    // 예: POST http://localhost:8080/api/self-diagnosis/specialty/gpt
    @PostMapping("/specialty/gpt")
    fun postGptSpecialtyPrediction(@RequestBody text: String): ResponseEntity<DiagnosisResult> {
        val response = selfDiagnosisService.diagnoseSpecialtyWithGpt(text)
        return ResponseEntity.ok(response)
    }
}
