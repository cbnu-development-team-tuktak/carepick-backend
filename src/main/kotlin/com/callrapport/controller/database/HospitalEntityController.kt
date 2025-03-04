package com.callrapport.controller

import com.callrapport.dto.HospitalDetailsResponse
import com.callrapport.model.hospital.Hospital
import com.callrapport.service.HospitalService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/hospitals")
class HospitalEntityController(
    private val hospitalService: HospitalService
) {
    // 병원명으로 검색 (페이징)
    @GetMapping("/search")
    fun searchHospitals(
        @RequestParam keyword: String, 
        pageable: Pageable
    ): Page<Hospital> {
        return hospitalService.searchHospitalsByName(keyword, pageable)
    }

    // 주소로 병원 검색 (페이징)
    @GetMapping("/search/address")
    fun searchHospitalsByAddress(
        @RequestParam keyword: String,
        pageable: Pageable
    ): Page<Hospital> {
        return hospitalService.searchHospitalsByAddress(keyword, pageable)
    }

    // 모든 병원 목록 조회 (페이징)
    @GetMapping
    fun getAllHospitals(pageable: Pageable): Page<HospitalDetailsResponse> {
        // 병원 데이터를 가져오고, HospitalDetailsResponse로 변환
        val hospitalPage = hospitalService.getAllHospitals(pageable)
        return hospitalPage.map { hospital -> 
            HospitalDetailsResponse.from(hospital)
        }
    }
}
