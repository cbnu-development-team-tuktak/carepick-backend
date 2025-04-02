package com.callrapport.controller

// DTO import 
import com.callrapport.dto.DiseaseDetailsResponse // 질병 정보 응답용 DTO

// Model (엔티티) 관련 import
import com.callrapport.model.disease.Disease // Disease: 가공된 질병 데이터 엔티티
import com.callrapport.model.disease.DiseaseRaw // DiseaseRaw: 원본 데이터 엔티티

// Repository (저장소) 관련 import
import com.callrapport.repository.disease.DiseaseRepository // 가공 질병 데이터 리포지토리
import com.callrapport.repository.disease.DiseaseRawRepository // 질병 원본 데이터 리포지토리

// Service (서비스) 관련 import
import com.callrapport.service.disease.DiseaseService

// Spring Data JPA 관련 import 
import org.springframework.data.domain.Page // 페이징된 응답을 위한 객체
import org.springframework.data.domain.PageImpl // 수동으로 Page 객체를 구성할 때 사용
import org.springframework.data.domain.Pageable // 페이징 정보(페이지 번호, 크기 등)를 담는 인터페이스

// Spring Web 관련 import
import org.springframework.web.bind.annotation.* // REST 컨트롤러, 매핑, 요청 파라미터 어노테이션 등
import org.springframework.http.ResponseEntity // HTTP 응답 객체

@RestController
@RequestMapping("/api/diseases")
class DiseaseEntityController(
    // 서비스
    private val diseaseService: DiseaseService, // 질병 정보 서비스

    // 리포지토리
    private val diseaseRepository: DiseaseRepository, // 가공된 질병 데이터 저장소
    private val diseaseRawRepository: DiseaseRawRepository // 원본 질병 데이터 저장소
) {

    // 전체 원본 질병 데이터 조회
    // ex) http://localhost:8080/api/diseases/raw
    @GetMapping("/raw")
    fun getAllRawDiseases(): ResponseEntity<List<DiseaseRaw>> {
        // 데이터베이스에 저장된 모든 DiseaseRaw 엔티티를 조회
        val rawList = diseaseRawRepository.findAll()

        // 조회된 개수를 로그로 출력 (디버깅 및 확인용)
        println("Retrieved ${rawList.size} raw diseases from DB")

        // HTTP 200 OK 상태와 함께 질병 리스트를 JSON 배열 형태로 응답
        return ResponseEntity.ok(rawList)
    }

    // 전체 원본 질병 데이터 삭제
    // ex) http://localhost:8080/api/diseases/raw/delete
    @GetMapping("/raw/delete")
    fun deleteAllRawDiseases(): ResponseEntity<String> {
        // 삭제 전 현재 저장된 원본 질병 데이터 수를 확인
        val count = diseaseRawRepository.count()

        // 원본 질병 데이터 전체 삭제 수행
        diseaseRawRepository.deleteAll()

        // 삭제된 개수를 로그로 출력
        println("Deleted $count raw diseases from DB")

        // 삭제 결과 메시지를 HTTP 200 OK와 함께 반환
        return ResponseEntity.ok("All $count raw disease records have been deleted.")
    }

    // 전체 가공된 질병 데이터를 페이지네이션으로 조회
    // 예: http://localhost:8080/api/diseases/processed?page=0&size=219
    @GetMapping("/processed")
    fun getAllDiseases(pageable: Pageable): Page<DiseaseDetailsResponse> {
        val diseasePage = diseaseService.getAllDiseases(pageable) // 서비스 계층 호출
        val dtoList = diseasePage.content.map { DiseaseDetailsResponse.from(it) } // 엔티티 → DTO 변환
        return PageImpl(dtoList, pageable, diseasePage.totalElements) // Page 객체로 응답
    }

    // 특정 가공 질병 데이터 조회
    // 예: http://localhost:8080/api/diseases/processed/1
    @GetMapping("/processed/{id}")
    fun getDiseaseById(@PathVariable id: Long): ResponseEntity<DiseaseDetailsResponse> {
        val disease = diseaseRepository.findById(id)

        return if (disease.isPresent) {
            val dto = DiseaseDetailsResponse.from(disease.get())
            ResponseEntity.ok(dto)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    // 전체 가공된 질병 데이터 삭제
    // ex) http://localhost:8080/api/diseases/processed/delete
    // @GetMapping("/processed/delete")
    // fun deleteAllProcessedDiseases(): ResponseEntity<String> {
    //     // 삭제 전에 현재 저장된 가공된 질병 데이터 수를 확인
    //     val count = diseaseRepository.count()

    //     // 가공된 질병 데이터 전체 삭제 수행
    //     diseaseRepository.deleteAll()

    //     // 삭제된 개수를 로그로 출력
    //     println("Deleted $count processed diseases from DB")

    //     // 삭제 결과 메시지를 HTTP 200 OK와 함께 반환
    //     return ResponseEntity.ok("All $count processed disease records have been deleted.")
    // }
}
