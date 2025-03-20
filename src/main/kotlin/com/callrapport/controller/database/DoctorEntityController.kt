package com.callrapport.controller

import com.callrapport.dto.DoctorDetailsResponse
import com.callrapport.service.DoctorService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/doctors")
class DoctorEntityController(
    private val doctorService: DoctorService
) {
    // 의사 이름으로 검색 (페이징) - DTO 적용
    @GetMapping("/search")
    fun searchDoctors(@RequestParam keyword: String, pageable: Pageable): Page<DoctorDetailsResponse> {
        val doctorPage = doctorService.searchDoctorsByName(keyword, pageable)
        val dtoList = doctorPage.content.map { DoctorDetailsResponse.from(it) } // ✅ DTO 변환
        return PageImpl(dtoList, pageable, doctorPage.totalElements)
    }

    // 모든 의사 목록 조회 (페이징) - DTO 적용
    @GetMapping
    fun getAllDoctors(pageable: Pageable): Page<DoctorDetailsResponse> {
        val doctorPage = doctorService.getAllDoctors(pageable)
        val dtoList = doctorPage.content.map { DoctorDetailsResponse.from(it) } // ✅ DTO 변환
        return PageImpl(dtoList, pageable, doctorPage.totalElements)
    }
}
