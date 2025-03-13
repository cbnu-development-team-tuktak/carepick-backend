package com.callrapport.repository.doctor

import com.callrapport.model.doctor.DoctorSpecialty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DoctorSpecialtyRepository : JpaRepository<DoctorSpecialty, Long> {
    fun findByDoctorId(doctorId: String): List<DoctorSpecialty>
    fun findBySpecialtyId(specialtyId: Long): List<DoctorSpecialty>
}
