package com.callrapport.controller

import com.callrapport.model.doctor.Doctor
import com.callrapport.repository.doctor.DoctorRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/doctors")
class DoctorController(private val doctorRepository: DoctorRepository) {
    // 모든 의사 조회 
    @GetMapping
    fun getAllDoctors(): ResponseEntity<List<Doctor>> {
        val results = doctorRepository.findAll()
        return ResponseEntity.ok(results)
    }

    // 의사 이름으로 검색
    @GetMapping("/search-by-name")
    fun getDoctorByName(@RequestParam name: String): ResponseEntity<Any> {
        val results = doctorRepository.findByNameContaining(name)
        return if (results.isNotEmpty()) {
            ResponseEntity.ok(results)
        } else {
            ResponseEntity.status(404).body(mapOf("message" to "해당 이름의 의사가 없습니다."))
        }
    }

    // 병원으로 검색
    @GetMapping("/by-hospital/{hospitalId}")
    fun getDoctorsByHospital(@PathVariable hospitalId: Long): ResponseEntity<Any> {
        val results = doctorRepository.findByHospitalId(hospitalId)
        return if (results.isNotEmpty()) {
            ResponseEntity.ok(results)
        } else {
            ResponseEntity.status(404).body(mapOf("message" to "해당 병원에 소속된 의사가 없습니다."))
        }
    }
}