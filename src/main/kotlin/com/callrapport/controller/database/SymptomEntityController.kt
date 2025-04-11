package com.callrapport.controller.disease

// DTO 및 서비스 관련 import
import com.callrapport.dto.SymptomDetailsResponse // 증상 정보 응답용 DTO
import com.callrapport.service.disease.SymptomService // 증상 관련 비즈니스 로직 처리 서비스

// Spring 관련 import
import org.springframework.data.domain.Page // 페이지 처리된 결과 객체
import org.springframework.data.domain.Pageable // 페이징 요청 정보를 담는 인터페이스
import org.springframework.http.ResponseEntity // HTTP 응답 객체
import org.springframework.web.bind.annotation.* // REST 컨트롤러 및 매핑 어노테이션

// 기타 import
import org.springframework.web.bind.annotation.RestController // REST 컨트롤러 클래스 정의용 어노테이션

@RestController
@RequestMapping("/api/symptoms")
class SymptomEntityController(
    private val symptomService: SymptomService // 증상 데이터를 처리하는 서비스
) {
    // 증상 개수 조회
    // 예: http://localhost:8080/api/symptoms/count
    @GetMapping("/count")
    fun getSymptomsCount(): ResponseEntity<Map<String, Long>> {
        val count = symptomService.countAllSymptoms()  // 전체 증상 개수 조회
        return ResponseEntity.ok(mapOf("count" to count))
    }
    
    // 전체 증상 목록 조회 (페이지 단위)
    // 예: http://localhost:8080/api/symptoms?page=0&size=10
    @GetMapping
    fun getAllSymptoms(pageable: Pageable): ResponseEntity<Page<SymptomDetailsResponse>> {
        val page = symptomService.getAllSymptoms(pageable)
            .map { SymptomDetailsResponse.from(it) } // DTO로 매핑

        return ResponseEntity.ok(page)
    }

    // 단일 증상 조회
    // 예: http://localhost:8080/api/symptoms/1008
    @GetMapping("/{id}")
    fun getSymptomById(@PathVariable id: Long): ResponseEntity<SymptomDetailsResponse> {
        val symptom = symptomService.getSymptomById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(SymptomDetailsResponse.from(symptom))
    }

    // 특정 초성 범위로 증상 목록 조회 (예: ㄱ → start=가, end=나)
    // 예: http://localhost:8080/api/symptoms/filter?start=가&end=나&page=0&size=10
    @GetMapping("/filter")
    fun getSymptomsByInitialRange(
        @RequestParam start: String,
        @RequestParam end: String,
        pageable: Pageable
    ): ResponseEntity<Page<SymptomDetailsResponse>> {
        val page = symptomService.getSymptomsByInitialRange(start, end, pageable)
            .map { SymptomDetailsResponse.from(it) }
        return ResponseEntity.ok(page)
    }

    // 특정 초성 범위로 증상 목록의 개수 조회 (예: ㄱ → start=가, end=나)
    // 예: http://localhost:8080/api/symptoms/filter/count?start=가&end=나
    @GetMapping("/filter/count")
    fun getSymptomsCountByInitialRange(
        @RequestParam start: String,
        @RequestParam end: String
    ): ResponseEntity<Map<String, Long>> {
        val count = symptomService.countSymptomsByInitialRange(start, end)
        return ResponseEntity.ok(mapOf("count" to count))
    }

    // 증상 삭제
    // 예: http://localhost:8080/api/symptoms/1008
    @DeleteMapping("/{id}")
    fun deleteSymptom(@PathVariable id: Long): ResponseEntity<Void> {
        val isDeleted = symptomService.deleteSymptom(id)
        return if (isDeleted) {
            ResponseEntity.noContent().build() // 삭제 성공
        } else {
            ResponseEntity.notFound().build() // 삭제할 증상이 없으면 404
        }
    }
}
