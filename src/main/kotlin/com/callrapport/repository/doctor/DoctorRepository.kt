package com.callrapport.repository.doctor

import com.callrapport.model.doctor.Doctor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DoctorRepository : JpaRepository<Doctor, Long> {
    fun findByNameContaining(name: String): List<Doctor> // 의사 이름으로 검색
    fun findBySpecialty(specialty: String): List<Doctor> // 전문 과목으로 검색
    fun findByHospitalId(hospitalId: Long): List<Doctor> // 병원으로 검색
}