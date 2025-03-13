package com.callrapport.repository.doctor

import com.callrapport.model.doctor.Doctor
import com.callrapport.model.doctor.DoctorCareer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DoctorCareerRepository : JpaRepository<DoctorCareer, Long> {
    // 특정 의사(Doctor)에 해당하는 모든 경력 정보를 조회하는 메서드
    fun findByDoctor(
        doctor: Doctor // 검색할 Doctor 객체
    ): List<DoctorCareer>?
}