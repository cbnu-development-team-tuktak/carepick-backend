package com.callrapport.repository.doctor

import com.callrapport.model.doctor.EducationLicense
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EducationLicenseRepository : JpaRepository<EducationLicense, Long> {
    fun findByName(name: String): EducationLicense? // ✅ 자격면허 이름으로 조회
}
