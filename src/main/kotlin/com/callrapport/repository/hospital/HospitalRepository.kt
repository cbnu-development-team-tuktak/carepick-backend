package com.callrapport.repository.hospital

import com.callrapport.model.hospital.Hospital
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HospitalRepository : JpaRepository<Hospital, String> {
    // 병원 이름으로 검색
    fun findByNameContaining(name: String): List<Hospital>

    // 특정 주소로 병원 검색
    fun findByAddressContaining(adress: String): List<Hospital>
}