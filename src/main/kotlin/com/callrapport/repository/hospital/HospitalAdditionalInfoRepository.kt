package com.callrapport.repository.hospital

import com.callrapport.model.hospital.Hospital
import com.callrapport.model.hospital.HospitalAdditionalInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HospitalAdditionalInfoRepository : JpaRepository<HospitalAdditionalInfo, String> {
     // 병원 정보를 기반으로 추가 정보 엔티티 조회
     fun findByHospital(hospital: Hospital): HospitalAdditionalInfo?
}
