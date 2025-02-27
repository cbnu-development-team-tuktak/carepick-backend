package com.callrapport.repository.hospital

import com.callrapport.model.hospital.Hospital
import com.callrapport.model.hospital.HospitalSpecialty
import com.callrapport.model.common.Specialty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HospitalSpecialtyRepository : JpaRepository<HospitalSpecialty, Long> {
    // 특정 병원의 모든 진료과 가져오기
    fun findByHospital(hospital: Hospital): List<HospitalSpecialty>
    
    // 특정 진료과를 갖고 있는 병원 검색
    fun funBySpecialty(specialty: Specialty): List<HospitalSpecialty>
}
