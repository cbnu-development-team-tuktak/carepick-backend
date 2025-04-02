package com.callrapport.controller.disease

// DTO 및 서비스 관련 import
import com.callrapport.dto.SymptomDetailsResponse
import com.callrapport.service.disease.SymptomService

// Spring 관련 import
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

// 기타 import
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/symptoms")
class SymptomEntityController(
    private val symptomService: SymptomService
) {

    // 전체 증상 목록 조회 (페이지 단위)
    // 예: http://localhost:8080/api/symptoms?page=0&size=10
    @GetMapping
    fun getAllSymptoms(pageable: Pageable): ResponseEntity<Page<SymptomDetailsResponse>> {
        val page = symptomService.getAllSymptoms(pageable)
            .map { SymptomDetailsResponse.from(it) } // DTO로 매핑

        return ResponseEntity.ok(page)
    }

    // 단일 증상 조회
    // 예: GET /api/symptoms/1008
    @GetMapping("/{id}")
    fun getSymptomById(@PathVariable id: Long): ResponseEntity<SymptomDetailsResponse> {
        val symptom = symptomService.getSymptomById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(SymptomDetailsResponse.from(symptom))
    }
}
