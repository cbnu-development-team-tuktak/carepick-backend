package com.callrapport.controller

// DTO import 
import com.callrapport.dto.HospitalDetailsResponse // 병원 정보 응답용 DTO

// 엔티티 import 
import com.callrapport.model.hospital.Hospital // 병원 엔티티

// 서비스 import 
import com.callrapport.service.HospitalService // 병원 관련 비즈니스 로직 처리 서비스 

// Spring Data JPA 관련 import
import org.springframework.data.domain.Page // 페이징 결과를 담는 객체
import org.springframework.data.domain.Pageable // 페이징 요청 정보를 담는 객체

// Spring Web 관련 import 
import org.springframework.http.ResponseEntity // HTTP 응답 객체
import org.springframework.web.bind.annotation.* // REST 컨트롤러 관련 어노테이션들

@RestController
@RequestMapping("/api/hospitals")
class HospitalEntityController(
    private val hospitalService: HospitalService // 병원 데이터를 처리하는 서비스 의존성 주입 
) {
    // 병원 개수 조회
    // 예: http://localhost:8080/api/hospitals/count
    @GetMapping("/count")
    fun getSymptomsCount(): ResponseEntity<Map<String, Long>> {
        val count = hospitalService.countAllHospitals()  // 전체 증상 개수 조회
        return ResponseEntity.ok(mapOf("count" to count))
    }


    // 병원명으로 검색
    // 예: http://localhost:8080/api/hospitals/search?keyword=베이드의원&page=0&size=10
    @GetMapping("/search")
    fun searchHospitals(
        @RequestParam keyword: String, // 검색 키워드 (병원 이름)
        pageable: Pageable // 페이징 정보
    ): Page<HospitalDetailsResponse> {
        // 병원명 기준 검색 후 DTO로 변환
        val hospitalPage = hospitalService.searchHospitalsByName(keyword, pageable)
        return hospitalPage.map { HospitalDetailsResponse.from(it) }
    }

    // 주소로 병원 검색
    // 예: http://localhost:8080/api/hospitals/search/address?keyword=강남&page=0&size=10
    @GetMapping("/search/address")
    fun searchHospitalsByAddress(
        @RequestParam keyword: String, // 검색 키워드 (주소 일부)
        pageable: Pageable // 페이징 정보
    ): Page<HospitalDetailsResponse> {
        // 주소 기준 검색 후 DTO로 변환
        val hospitalPage = hospitalService.searchHospitalsByAddress(keyword, pageable)
        return hospitalPage.map { HospitalDetailsResponse.from(it) }
    }

    // 모든 병원 목록 조회 (DTO 반환)
    // 예: http://localhost:8080/api/hospitals?page=0&size=10
    @GetMapping
    fun getAllHospitals(
        pageable: Pageable // 페이징 정보
    ): Page<HospitalDetailsResponse> {
        // 전체 병원 조회 후 DTO로 변환
        val hospitalPage = hospitalService.getAllHospitals(pageable)
        return hospitalPage.map { HospitalDetailsResponse.from(it) }
    }

    // 병원 ID로 단일 병원 조회
    // 예: http://localhost:8080/api/hospitals/H0000238449
    @GetMapping("/{id}")
    fun getHospitalById(
        @PathVariable id: String
    ): HospitalDetailsResponse {
        // 서비스 계층을 통해 병원 엔티티 조회
        val hospital = hospitalService.getHospitalById(id)
        // 조회된 병원 엔티티를 DTO로 변환하여 응답
        return HospitalDetailsResponse.from(hospital)
    }
}
