package com.callrapport.controller.disease

// Service 관련 import
import com.callrapport.service.disease.DiseaseService

// DTO 관련 import
import com.callrapport.dto.DiseaseDetailsResponse

// Spring 관련 import
import org.springframework.web.bind.annotation.*

// Spring Data JPA 관련 import
import org.springframework.data.domain.Page // 페이징 결과를 담는 객체
import org.springframework.data.domain.Pageable // 페이징 요청 정보를 담는 객체
import org.springframework.data.web.PageableDefault

import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api/diseases")
class DiseaseController(
    private val diseaseService: DiseaseService
) {
    // 질병 CSV 파일을 읽어 초기화
    // 예: http://localhost:8080/api/diseases/initialize
    // @GetMapping("/initialize")
    // fun initializeDiseases(): ResponseEntity<String> {
    //     val filePath = "csv/disease_list.csv"
    //     diseaseService.saveDiseasesFromCsv(filePath)
    //     return ResponseEntity.ok("질병 데이터가 성공적으로 저장되었습니다.")
    // }

    // 전체 질병 목록 조회
    // 예: http://localhost:8080/api/diseases?page=0&size=20
    @GetMapping
    fun getAllDiseases(
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<DiseaseDetailsResponse> {
        return diseaseService.getAllDiseases(pageable)
            .map { DiseaseDetailsResponse.from(it) }
    }

    // 질병명으로 검색
    // 예: http://localhost:8080/api/diseases/search/name?name=고혈압
    @GetMapping("/search/name")
    fun searchByName(
        @RequestParam name: String,
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<DiseaseDetailsResponse> {
        return diseaseService.searchByName(name, pageable)
            .map { DiseaseDetailsResponse.from(it) }
    }

    // 분류명으로 검색
    // 예: http://localhost:8080/api/diseases/search/category?category=호흡기계%20질환
    @GetMapping("/search/category")
    fun searchByCategoryName(
        @RequestParam category: String,
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<DiseaseDetailsResponse> {
        return diseaseService.searchByCategoryName(category, pageable)
            .map { DiseaseDetailsResponse.from(it) }
    }

    // 신체계통으로 검색
    // 예: http://localhost:8080/api/diseases/search/body-system?system=순환기
    @GetMapping("/search/body-system")
    fun searchByBodySystemName(
        @RequestParam system: String,
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<DiseaseDetailsResponse> {
        return diseaseService.searchByBodySystemName(system, pageable)
            .map { DiseaseDetailsResponse.from(it) }
    }

    // 진료과로 검색
    // 예: http://localhost:8080/api/diseases/search/specialty?specialty=내과
    @GetMapping("/search/specialty")
    fun searchBySpecialtyName(
        @RequestParam specialty: String,
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<DiseaseDetailsResponse> {
        return diseaseService.searchBySpecialtyName(specialty, pageable)
            .map { DiseaseDetailsResponse.from(it) }
    }
} 
