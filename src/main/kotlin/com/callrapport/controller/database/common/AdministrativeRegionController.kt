package com.callrapport.controller.database

// Model (엔티티) 관련 import
import com.callrapport.model.common.AdministrativeRegion // 행정구역 엔티티

// DTO 관련 import
import com.callrapport.dto.SidoDetailsResponse // 시도 상세 응답 DTO
import com.callrapport.dto.SggDetailsResponse // 시군구 상세 응답 DTO
import com.callrapport.dto.UmdDetailsResponse // 읍면동 상세 응답 DTO

// Repository (저장소) 관련 import
import com.callrapport.repository.common.AdministrativeRegionRepository // 행정구역 리포지토리

// Service (서비스) 관련 import 
import com.callrapport.service.map.AdministrativeRegionService // 행정구역 서비스
import com.callrapport.service.crawl.AdministrativeRegionCrawlService // 행정구역 크롤링 서비스

// Spring Web 관련 import
import org.springframework.web.bind.annotation.* // REST 컨트롤러, 매핑, 요청 파라미터 어노테이션 등
import org.springframework.data.domain.Pageable // 페이징 요청 파라미터
import org.springframework.data.domain.Page // 페이징 응답 객체

// HTTP 응답 관련 import
import org.springframework.http.ResponseEntity // HTTP 응답 객체

@RestController
@RequestMapping("/api/administrative-regions")
class AdministrativeRegionController(
    private val administrativeRegionService: AdministrativeRegionService, // 행정구역 저장 로직을 담당하는 서비스
    private val administrativeRegionCrawlService: AdministrativeRegionCrawlService, // 행정구역 크롤링 로직을 담당하는 서비스
    private val administrativeRegionRepository: AdministrativeRegionRepository // 행정구역 조회/삭제를 담당하는 JPA 리포지토리
) {
    // 시도 목록을 크롤링하여 CSV로 저장하는 엔드포인트
    // 예: http://localhost:8080/api/administrative-regions/crawl/sido
    @GetMapping("/crawl/sido")
    fun crawlSido(): ResponseEntity<String> {
        return try {
            administrativeRegionCrawlService.crawlSidoList() // 시도 목록 크롤링 실행
            ResponseEntity.ok("✅ Sido crawl complete and saved to csv")
        } catch (e: Exception) {
            // 오류 발생 시 로그 출력 및 실패 응답 반환
            println("❌ Error during Sido crawl: ${e.message}")
            ResponseEntity.internalServerError()
                .body("❌ Error during Sido crawl: ${e.message}")
        }
    }

    // 시군구 목록을 크롤링하여 CSV로 저장하는 엔드포인트
    // 예: http://localhost:8080/api/administrative-regions/crawl/sgg
    @GetMapping("/crawl/sgg")
    fun crawlSgg(): ResponseEntity<String> {
        return try {
            administrativeRegionCrawlService.crawlSggList() // 시군구 목록 크롤링 실행
            ResponseEntity.ok("✅ Sgg crawl complete and saved to csv")
        } catch (e: Exception) {
            // 오류 발생 시 로그 출력 및 실패 응답 반환
            println("❌ Error during Sgg crawl: ${e.message}")
            ResponseEntity.internalServerError()
                .body("❌ Error during Sgg crawl: ${e.message}")
        }
    }

    // 읍면동 목록을 크롤링하는 엔드포인트
    // 예: http://localhost:8080/api/administrative-regions/crawl/umd
    @GetMapping("/crawl/umd")
    fun crawlUmd(): ResponseEntity<String> {
        return try {
            administrativeRegionCrawlService.crawlUmdList()
            ResponseEntity.ok("✅ Umd crawl complete and saved to csv")
        } catch (e: Exception) {
            println("❌ Error during Umd crawl: ${e.message}")
            ResponseEntity.internalServerError().body("❌ Error during Umd crawl: ${e.message}")
        }
    }

    // 모든 행정구역 데이터를 삭제하는 엔드포인트
    // 예: http://localhost:8080/api/administrative-regions/clear
    @GetMapping("/clear")
    fun clearData(): ResponseEntity<String> {
        return try {
            administrativeRegionService.clear() // 모든 행정구역 데이터 삭제
            ResponseEntity.ok("✅ All administrative region data cleared successfully")
        } catch (e: Exception) {
            println("❌ Error clearing administrative region data: ${e.message}")
            ResponseEntity.internalServerError().body("❌ Error clearing administrative region data: ${e.message}")
        }
    }

    // 시도 목록을 CSV 파일에서 읽어와 데이터베이스에 저장하는 엔드포인트
    // 예: http://localhost:8080/api/administrative-regions/sido/save
    @GetMapping("/sido/save")
    fun saveSidoList(): ResponseEntity<String> {
        return try {
            administrativeRegionService.saveSidoList() // 시도 목록 저장
            ResponseEntity.ok("✅ Sido list saved successfully")
        } catch (e: Exception) {
            println("❌ Error saving Sido list: ${e.message}")
            ResponseEntity.internalServerError().body("❌ Error saving Sido list: ${e.message}")
        }
    }

    // 시군구 목록을 CSV 파일에서 읽어와 데이터베이스에 저장하는 엔드포인트
    // 예: http://localhost:8080/api/administrative-regions/sgg/save
    @GetMapping("/sgg/save")
    fun saveSggList(): ResponseEntity<String> {
        return try {
            administrativeRegionService.saveSggList() // 시군구 목록 저장
            ResponseEntity.ok("✅ Sgg list saved successfully")
        } catch (e: Exception) {
            println("❌ Error saving Sgg list: ${e.message}")
            ResponseEntity.internalServerError().body("❌ Error saving Sgg list: ${e.message}")
        }
    }

    // 읍면동 목록을 CSV 파일에서 읽어와 데이터베이스에 저장하는 엔드포인트
    // 예: http://localhost:8080/api/administrative-regions/umd/save
    @GetMapping("/umd/save")
    fun saveUmdList(): ResponseEntity<String> {
        return try {
            administrativeRegionService.saveUmdList() // 읍면동 목록 저장
            ResponseEntity.ok("✅ Umd list saved successfully")
        } catch (e: Exception) {
            println("❌ Error saving Umd list: ${e.message}")
            ResponseEntity.internalServerError().body("❌ Error saving Umd list: ${e.message}")
        }
    }

    // 전국 시/도 상세 목록을 페이지 단위로 조회
    // 예: http://localhost:8080/api/administrative-regions/sidos?page=0&size=10
    @GetMapping("/sidos")
    fun getSidoList(pageable: Pageable): Page<SidoDetailsResponse> {
        // 행정구역 서비스에서 시도 목록을 페이지 단위로 조회
        return administrativeRegionService.getSidoList(pageable)
    }

    // 전국 시/군/구 상세 목록을 페이지 단위로 조회
    // 예: http://localhost:8080/api/administrative-regions/sggs?page=0&size=1000
    @GetMapping("/sggs")
    fun getSggList(pageable: Pageable): Page<SggDetailsResponse> {
        // 행정구역 서비스에서 시군구 목록을 페이지 단위로 조회
        return administrativeRegionService.getSggList(pageable)
    }

    // 전국 읍/면/동 상세 목록을 페이지 단위로 조회
    // 예: http://localhost:8080/api/administrative-regions/umds?page=0&size=10
    @GetMapping("/umds")
    fun getUmdList(pageable: Pageable): Page<UmdDetailsResponse> {
        // 행정구역 서비스에서 읍면동 목록을 페이지 단위로 조회
        return administrativeRegionService.getUmdList(pageable)
    }

    // 특정 시/도에 속한 시/군/구 상세 목록을 페이지 단위로 조회
    // 예: http://localhost:8080/api/administrative-regions/sggs/by-sido?sido=서울특별시&page=0&size=10
    @GetMapping("/sggs/by-sido")
    fun getSggListBySido(
        @RequestParam sido: String, // 요청 파라미터로 시도 이름을 받음
        pageable: Pageable // 페이징 정보를 담는 Pageable 객체
    ): Page<SggDetailsResponse> {
        // 행정구역 서비스에서 시도 이름과 페이징 정보를 받아 시군구 목록을 조회
        return administrativeRegionService.getSggListBySido(sido, pageable)
    }

    // 특정 시/군/구에 속한 읍/면/동 상세 목록을 페이지 단위로 조회
    // 예: http://localhost:8080/api/administrative-regions/umds/by-sgg?sgg=강남구&page=0&size=10
    @GetMapping("/umds/by-sgg")
    fun getUmdListBySgg(
        @RequestParam sgg: String, // 요청 파라미터로 시군구 이름을 받음
        pageable: Pageable // 페이징 정보를 담는 Pageable 객체
    ): Page<UmdDetailsResponse> {
        // 행정구역 서비스에서 시군구 이름과 페이징 정보를 받아 읍면동 목록을 조회
        return administrativeRegionService.getUmdListBySgg(sgg, pageable)
    }
}