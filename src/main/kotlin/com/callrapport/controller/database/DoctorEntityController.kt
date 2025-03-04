package com.callrapport.controller

import com.callrapport.dto.DoctorDetailsResponse
import com.callrapport.model.doctor.Doctor
import com.callrapport.service.DoctorService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/doctors")
class DoctorEntityController(
    private val doctorService: DoctorService
) {
    // 의사 이름으로 검색 (페이징)
    @GetMapping("/search")
    fun searchDoctors(@RequestParam keyword: String, pageable: Pageable): Page<Doctor> {
        return doctorService.searchDoctorsByName(keyword, pageable)
    }

    // 모든 의사 목록 조회 (페이징)
    @GetMapping
    fun getAllDoctors(pageable: Pageable): Page<Doctor> {
        return doctorService.getAllDoctors(pageable)
    }
}
