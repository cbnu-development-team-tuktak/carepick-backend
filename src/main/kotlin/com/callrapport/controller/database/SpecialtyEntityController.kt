package com.callrapport.controller

import com.callrapport.model.common.Specialty
import com.callrapport.repository.common.SpecialtyRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/specialties")
class SpecialtyEntityController(
    private val specialtyRepository: SpecialtyRepository
) {
    // 초기 진료과 데이터를 저장
    @GetMapping("/initialize")
    fun initializeSpecialties(): ResponseEntity<Map<String, Any>> {
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

        // 중복 저장 방지
        val savedSpecialties = specialties.filterNot { specialtyRepository.existsById(it.id) }
        specialtyRepository.saveAll(savedSpecialties)

        return ResponseEntity.ok(
            mapOf(
                "status" to "Specialties initialized successfully",
                "totalSaved" to savedSpecialties.size
            )
        )
    }

    // 전체 진료과 목록 조회 API
    @GetMapping
    fun getAllSpecialties(): ResponseEntity<List<Specialty>> {
        return ResponseEntity.ok(specialtyRepository.findAll())
    }
}
