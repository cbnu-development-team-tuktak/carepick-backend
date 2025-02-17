package com.callrapport.controller.hospital

import com.callrapport.model.hospital.Hospital
import com.callrapport.repository.hospital.HospitalRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/hospitals")
class HospitalController(private val hospitalRepository: HospitalRepository) {

    // 모든 병원 조회
    @GetMapping
    fun getAllHospitals(): ResponseEntity<List<Hospital>> {
        val hospitals = hospitalRepository.findAll()
        return ResponseEntity.ok(hospitals)
    }

    // 병원명으로 검색
    @GetMapping("/search-by-name")
    fun getHospitalByName(@RequestParam name: String): ResponseEntity<Any> {
        val results = hospitalRepository.findByNameContaining(name)
        return if (results.isNotEmpty()) {
            ResponseEntity.ok(results)
        } else {
            ResponseEntity.status(404).body(mapOf("message" to "해당 이름의 병원이 없습니다."))
        }
    }
}
