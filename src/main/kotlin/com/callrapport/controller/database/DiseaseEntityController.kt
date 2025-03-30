package com.callrapport.controller

// Model (엔티티) 관련 import
import com.callrapport.model.disease.Disease // Disease: 가공된 질병 데이터 엔티티
import com.callrapport.model.disease.DiseaseRaw // DiseaseRaw: 원본 데이터 엔티티

// Repository (저장소) 관련 import
import com.callrapport.repository.disease.DiseaseRepository // 가공 질병 데이터 리포지토리
import com.callrapport.repository.disease.DiseaseRawRepository // 질병 원본 데이터 리포지토리

// Spring Web 관련 import
import org.springframework.web.bind.annotation.* // REST 컨트롤러, 매핑, 요청 파라미터 어노테이션 등
import org.springframework.http.ResponseEntity // HTTP 응답 객체

@RestController
@RequestMapping("/api/diseases")
class DiseaseEntityController(
    private val diseaseRepository: DiseaseRepository, // 가공된 질병 데이터 저장소
    private val diseaseRawRepository: DiseaseRawRepository, // 원본 질병 데이터 저장소
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

    // 전체 가공된 질병 데이터 조회
    // ex) http://localhost:8080/api/diseases/processed
    @GetMapping("/processed")
    fun getAllProcessedDiseases(): ResponseEntity<List<Disease>> {
        // DB에서 모든 가공한 질병(Disease) 엔티티 조회
        val processedList = diseaseRepository.findAll()
        
        // 조회된 가공 질병 개수를 로그로 출력
        println("Retrieved ${processedList.size} processed diseases from DB")

        // HTTP 200 OK와 함께 JSON 배열로 응답
        return ResponseEntity.ok(processedList)
    }

    // 전체 가공된 질병 데이터 삭제
    // ex) http://localhost:8080/api/diseases/processed/delete
    @GetMapping("/processed/delete")
    fun deleteAllProcessedDiseases(): ResponseEntity<String> {
        // 삭제 전에 현재 저장된 가공된 질병 데이터 수를 확인
        val count = diseaseRepository.count()

        // 가공된 질병 데이터 전체 삭제 수행
        diseaseRepository.deleteAll()

        // 삭제된 개수를 로그로 출력
        println("Deleted $count processed diseases from DB")

        // 삭제 결과 메시지를 HTTP 200 OK와 함께 반환
        return ResponseEntity.ok("All $count processed disease records have been deleted.")
    }
}
