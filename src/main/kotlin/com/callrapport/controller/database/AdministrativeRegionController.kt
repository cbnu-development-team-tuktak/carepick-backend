package com.callrapport.controller.database

// Model (엔티티) 관련 import
import com.callrapport.model.common.AdministrativeRegion // 행정구역 엔티티

// Repository (저장소) 관련 import
import com.callrapport.repository.common.AdministrativeRegionRepository // 행정구역 리포지토리

// Service (서비스) 관련 import 
import com.callrapport.service.map.AdministrativeRegionService // 행정구역 서비스

// Spring Web 관련 import
import org.springframework.web.bind.annotation.* // REST 컨트롤러, 매핑, 요청 파라미터 어노테이션 등
import org.springframework.data.domain.Pageable // 페이징 요청 파라미터
import org.springframework.data.domain.Page // 페이징 응답 객체

@RestController
@RequestMapping("/api/administrative-regions")
class AdministrativeRegionController(
    private val administrativeRegionService: AdministrativeRegionService, // 행정구역 저장 로직을 담당하는 서비스
    private val administrativeRegionRepository: AdministrativeRegionRepository // 행정구역 조회/삭제를 담당하는 JPA 리포지토리
) {

    // 행정구역 CSV 파일을 읽어 DB에 저장
    // http://localhost:8080/api/administrative-regions/initialize
    // @GetMapping("/initialize")
    // fun initialize(): String {
    //     administrativeRegionService.saveAdministrativeRegion()
    //     return "행정구역 데이터가 성공적으로 저장되었습니다."
    // }

    // 모든 행정구역 데이터를 페이지로 조회
    // 예: http://localhost:8080/api/administrative-regions?page=0&size=10
    @GetMapping
    fun getAll(
        pageable: Pageable // 페이지 정보 (page, size, sort)
    ): Page<AdministrativeRegion> {
        return administrativeRegionService.findAll(pageable)
    }

    // 시도명으로 행정구역 데이터를 페이지로 조회
    // 예: http://localhost:8080/api/administrative-regions/sido?sido=경기도&page=0&size=10
    @GetMapping("/sido")
    fun getBySido(
        @RequestParam sido: String, // 시도명 (예: 경기도)
        pageable: Pageable // 페이지 정보 (page, size, sort)
    ): Page<AdministrativeRegion> {
        return administrativeRegionService.findBySido(sido, pageable)
    }

    // 시군구명으로 행정구역 데이터를 페이지로 조회
    // 예: http://localhost:8080/api/administrative-regions/sgg?sgg=김해시&page=0&size=10
    @GetMapping("/sgg")
    fun getBySgg(
        @RequestParam sgg: String, // 시군구명 (예: 김해시)
        pageable: Pageable // 페이지 정보 (page, size, sort)
    ): Page<AdministrativeRegion> {
        return administrativeRegionService.findBySgg(sgg, pageable)
    }

    // 읍면동명으로 행정구역 데이터를 페이지로 조회
    // 예: http://localhost:8080/api/administrative-regions/umd?umd=고덕동&page=0&size=10
    @GetMapping("/umd")
    fun getByUmd(
        @RequestParam umd: String, // 읍면동명 (예: 고덕동)
        pageable: Pageable // 페이지 정보 (page, size, sort)
    ): Page<AdministrativeRegion> {
        return administrativeRegionService.findByUmd(umd, pageable)
    }

    // 리명으로 행정구역 데이터를 페이지로 조회
    // 예: http://localhost:8080/api/administrative-regions/ri?ri=서리&page=0&size=10
    @GetMapping("/ri")
    fun getByRi(
        @RequestParam ri: String, // 리명 (예: 서리)
        pageable: Pageable // 페이지 정보 (page, size, sort)
    ): Page<AdministrativeRegion> {
        return administrativeRegionService.findByRi(ri, pageable)
    }

    // 시도명만 반환하는 엔드포인트 추가
    @GetMapping("/sido-names")
    fun getSidoNames(): List<String> {
        return administrativeRegionService.findSidoNames() // 페이징 없이 시도명만 반환
    }   

    // 시도명으로 읍면동 목록을 반환
    @GetMapping("/sido/umd")
    fun getUmdBySido(
        @RequestParam sido: String, // 시도명 (예: 경기도)
        pageable: Pageable // 페이지 정보 (page, size, sort)
    ): List<String> {
        return administrativeRegionService.findUmdBySido(sido, pageable)
    }
}