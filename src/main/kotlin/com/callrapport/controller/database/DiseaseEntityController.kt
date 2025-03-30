package com.callrapport.controller

// Model (엔티티) 관련 import
import com.callrapport.model.disease.DiseaseRaw // 질병 원본 데이터 엔티티

// Repository (저장소) 관련 import
import com.callrapport.repository.disease.DiseaseRawRepository // 질병 원본 데이터 레포지토리

// Spring Web 관련 import
import org.springframework.web.bind.annotation.* // REST 컨트롤러, 매핑, 요청 파라미터 어노테이션 등
import org.springframework.http.ResponseEntity // HTTP 응답 객체

@RestController
@RequestMapping("/api/diseases")
class DiseaseEntityController(
    private val diseaseRawRepository: DiseaseRawRepository // 질병 원본 데이터 저장소 주입
) {

    // 전체 질병 데이터 조회
    // ex) GET http://localhost:8080/api/diseases
    @GetMapping
    fun getAllDiseases(): ResponseEntity<List<DiseaseRaw>> {
        // 데이터베이스에 저장된 모든 질병 원본 정보를 조회
        val allDiseases = diseaseRawRepository.findAll()

        // 조회된 질병 수를 로그로 추력
        println("📦 Retrieved ${allDiseases.size} diseases from DB")

        // 조회된 데이터를 HTTP 200 OK와 함께 JSON 배열 형태로 반환
        return ResponseEntity.ok(allDiseases)
    }

    // 전체 질병 데이터 삭제
    // ex) GET http://localhost:8080/api/diseases/delete
    @GetMapping("/delete")
    fun deleteAllDiseases(): ResponseEntity<String> {
        // 삭제 전에 기존 질병 개수를 카운트하여 로그 및 응답에 활용
        val count = diseaseRawRepository.count()

        // 데이터베이스에 모든 질병 원본 데이터를 삭제
        diseaseRawRepository.deleteAll()

        // 삭제된 데이터 개수를 로그로 출력
        println("🗑️ Deleted $count diseases from DB")

        // 삭제 결과를 메시지로 반환 (HTTP 200 OK)
        return ResponseEntity.ok("🗑️ All $count disease records have been deleted.")
    }
}
