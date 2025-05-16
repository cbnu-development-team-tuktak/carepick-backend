package com.callrapport.controller.disease

// Service 관련 import
import com.callrapport.service.disease.DiseaseService // 질병 관련 비즈니스 로직 서비스

// DTO 관련 import
import com.callrapport.dto.DiseaseDetailsResponse // 질병 정보 응답 DTO

// Spring 관련 import
import org.springframework.web.bind.annotation.* // REST API 컨트롤러 및 라우팅 어노테이션

// Spring Data JPA 관련 import
import org.springframework.data.domain.Page // 페이징 결과를 담는 객체
import org.springframework.data.domain.Pageable // 페이징 요청 정보를 담는 객체
import org.springframework.data.web.PageableDefault // 기본 페이지 설정 어노테이션

// HTTP 응답 관련 import
import org.springframework.http.ResponseEntity // HTTP 응답 본문 및 상태 코드를 표현하는 객체

@RestController
@RequestMapping("/api/diseases")
class DiseaseController(
    // 질병 비즈니스 로직을 처리하는 서비스 의존성 주입
    private val diseaseService: DiseaseService
) {
    // 질병 CSV 파일을 읽어 초기화
    // 예: http://localhost:8080/api/diseases/initialize
    // @GetMapping("/initialize")
    // fun initializeDiseases(): ResponseEntity<String> {
    //     // CSV 파일 경로 지정
    //     val filePath = "csv/disease_list.csv" 

    //     // 지정된 CSV 파일로부터 질병 데이터를 저장
    //     diseaseService.saveDiseasesFromCsv(filePath) 
        
    //     // 처리 완료 메시지 반환
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

    // 질병명으로 기준으로 질병 목록을 검색
    // 예: http://localhost:8080/api/diseases/search/name?name=고혈압
    @GetMapping("/search/name")
    fun searchByName(
        @RequestParam name: String, // 검색할 질병명 (예: 감기)
        @PageableDefault(size = 20) pageable: Pageable // 페이지 크기 (기본값: 20)
    ): Page<DiseaseDetailsResponse> {
        return diseaseService.searchByName(name, pageable) // 질병명으로 검색
            .map { DiseaseDetailsResponse.from(it) } // 검색 결과를 DTO로 변환
    }

    // 분류명으로 기준으로 질병 목록을 검색
    // 예: http://localhost:8080/api/diseases/search/category?category=호흡기계%20질환
    @GetMapping("/search/category")
    fun searchByCategoryName(
        @RequestParam category: String, // 검색할 분류명 (예: 호흡기 질환)
        @PageableDefault(size = 20) pageable: Pageable // 페이지 크기 (기본값: 20)
    ): Page<DiseaseDetailsResponse> {
        return diseaseService.searchByCategoryName(category, pageable) // 분류명으로 검색
            .map { DiseaseDetailsResponse.from(it) } // 검색 결과를 DTO로 변환
    }

    // 신체계통으로 검색
    // 예: http://localhost:8080/api/diseases/search/body-system?system=순환기
    @GetMapping("/search/body-system")
    fun searchByBodySystemName(
        @RequestParam system: String, // 검색할 신체계통명 (예: 순환기)
        @PageableDefault(size = 20) pageable: Pageable // 페이지 크기 (기본값: 20)
    ): Page<DiseaseDetailsResponse> {
        return diseaseService.searchByBodySystemName(system, pageable) // 신체계통명으로 검색
            .map { DiseaseDetailsResponse.from(it) } // 검색 결과를 DTO로 변환
    }

    // 진료과로 검색
    // 예: http://localhost:8080/api/diseases/search/specialty?specialty=내과
    @GetMapping("/search/specialty")
    fun searchBySpecialtyName(
        @RequestParam specialty: String, // 검색할 진료과명 (예: 내과)
        @PageableDefault(size = 20) pageable: Pageable // 페이지 크기 (기본값: 20)
    ): Page<DiseaseDetailsResponse> {
        return diseaseService.searchBySpecialtyName(specialty, pageable) // 진료과명으로 검색
            .map { DiseaseDetailsResponse.from(it) } // 검색 결과를 DTO로 변환
    }
} 
