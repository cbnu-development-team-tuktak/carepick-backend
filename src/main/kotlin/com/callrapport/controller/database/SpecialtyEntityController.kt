package com.callrapport.controller

// 엔티티 import
import com.callrapport.model.common.Specialty // 진료과 엔티티
// 레포지토리 import
import com.callrapport.repository.common.SpecialtyRepository // 진료과 정보를 저장/조회하는 JPA 레포지토리
import org.springframework.http.ResponseEntity // HTTP 응답을 표현하는 클래스  
import org.springframework.web.bind.annotation.* // Rest 컨트롤러 및 매핑 어노테이션

@RestController
@RequestMapping("/api/specialties")
class SpecialtyEntityController(
    private val specialtyRepository: SpecialtyRepository // 진료과 레포지토리
) {
    // 초기 진료과 데이터를 저장
    // 예: http://localhost:8080/api/specialties/initialize 
    @GetMapping("/initialize")
    fun initializeSpecialties(): ResponseEntity<Map<String, Any>> {
        // 초기 진료과 리스트 정리 (진료과 ID + 이름)
        val specialties = listOf(
            Specialty("PF000", "가정의학과"),
            Specialty("PM000", "내과"),
            Specialty("PT000", "마취통증의학과"),
            Specialty("PX000", "방사선종양학과"),
            Specialty("PW000", "병리과"),
            Specialty("PU000", "비뇨의학과"),
            Specialty("PY000", "산부인과"),
            Specialty("PI000", "산업의학과"),
            Specialty("PA000", "성형외과"),
            Specialty("PD000", "소아청소년과"),
            Specialty("PN000", "신경과"),
            Specialty("PB000", "신경외과"),
            Specialty("PH000", "안과"),
            Specialty("PK000", "영상의학과"),
            Specialty("PZ000", "예방의학과"),
            Specialty("PG000", "외과"),
            Specialty("PJ000", "응급의학과"),
            Specialty("PE000", "이비인후과"),
            Specialty("PR000", "재활의학과"),
            Specialty("PP000", "정신건강의학과"),
            Specialty("PO000", "정형외과"),
            Specialty("PQ000", "직업환경의학과"),
            Specialty("PQL00", "진단검사의학과"),
            Specialty("PV000", "치과"),
            Specialty("PS000", "피부과"),
            Specialty("PL000", "한방과"),
            Specialty("PXN00", "핵의학과"),
            Specialty("PC000", "흉부외과")
        )

        // 이미 DB에 존재하는 진료과는 제외하고 저장
        val savedSpecialties = specialties.filterNot { specialtyRepository.existsById(it.id) }
        specialtyRepository.saveAll(savedSpecialties)

        // 성공 응답 반환
        return ResponseEntity.ok(
            mapOf(
                "status" to "Specialties initialized successfully", // 상태 메시지
                "totalSaved" to savedSpecialties.size // 새로 저장된 진료과 수
            )
        )
    }

    // 전체 진료과 목록 조회 
    // 예: http://localhost:8080/api/specialties
    @GetMapping
    fun getAllSpecialties(): ResponseEntity<List<Specialty>> {
        // DB에 저장된 모든 진료과 목록 조회 및 반환
        return ResponseEntity.ok(specialtyRepository.findAll())
    }

    // 전체 진료과 데이터를 삭제
    // 예: http://localhost:8080/api/specialties/delete
    // @GetMapping("/delete")
    // fun deleteAllSpecialties(): ResponseEntity<String> {
    //     val count = specialtyRepository.count() // 삭제 전 개수 확인
    //     specialtyRepository.deleteAll() // 전체 삭제 수행
    //     return ResponseEntity.ok("✅ $count specialties have been deleted from the database.")
    // }
}
