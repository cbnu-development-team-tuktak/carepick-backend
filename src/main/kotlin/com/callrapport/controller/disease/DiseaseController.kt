package com.callrapport.controller.disease

import com.callrapport.model.disease.Disease
import com.callrapport.repository.disease.DiseaseRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/diseases")
class DiseaseController(private val diseaseRepository: DiseaseRepository) {
    // 모든 질병 조회
    @GetMapping
    fun getAllDiseases(): ResponseEntity<List<Disease>> {
        val results = diseaseRepository.findAll()
        return ResponseEntity.ok(results)
    }

    // 질병명(한국어)으로 검색
    @GetMapping("/search-by-name-kr")
    fun getDiseaseByNameKr(@RequestParam nameKr: String): ResponseEntity<Any> {
        val results = diseaseRepository.findByNameKrContaining(nameKr)
        return if (results.isNotEmpty()) {
            ResponseEntity.ok(results)
        } else {
            ResponseEntity.status(404).body(mapOf("message" to "검색 결과가 없습니다."))
        }
    }

    // 질병명(영어)으로 검색 
    @GetMapping("/search-by-name-en")
    fun getDiseaseByNameEn(@RequestParam nameEn: String): ResponseEntity<Any> {
        val results = diseaseRepository.findByNameEnContaining(nameEn)
        return if (results.isNotEmpty()) {
            ResponseEntity.ok(results)
        } else {
            ResponseEntity.status(404).body(mapOf("message" to "검색 결과가 없습니다."))
        }
    }

    // 질병 코드로 검색
    @GetMapping("/by-code/{diseaseCode}")
    fun getDiseaseByDiseaseCode(@PathVariable diseaseCode: String): ResponseEntity<Any> {
        var result = diseaseRepository.findByDiseaseCode(diseaseCode)
        return if (result != null) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.status(404).body(mapOf("message" to "해당 질병 코드의 데이터가 없습니다."))
        }
    }
}
