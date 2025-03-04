package com.callrapport.repository.hospital

import com.callrapport.model.hospital.Hospital
import com.callrapport.model.hospital.HospitalDoctor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HospitalDoctorRepository : JpaRepository<HospitalDoctor, Long> {
    fun findByHospital(hospital: Hospital): List<HospitalDoctor> 
}