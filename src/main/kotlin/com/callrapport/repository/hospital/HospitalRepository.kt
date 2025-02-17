package com.callrapport.repository.hospital

import com.callrapport.model.hospital.Hospital
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HospitalRepository : JpaRepository<Hospital, Long> {
    fun findByNameContaining(name: String): List<Hospital> // 병원명으로 검색
}
