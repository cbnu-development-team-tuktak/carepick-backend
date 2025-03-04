package com.callrapport.repository.hospital

import com.callrapport.model.hospital.HospitalAdditionalInfo
import com.callrapport.model.hospital.Hospital
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HospitalAdditionalInfoRepository : JpaRepository<HospitalAdditionalInfo, String> {
    fun findByHospitalId(hospitalId: String): HospitalAdditionalInfo?
    
    // 24시간 운영하는 병원 목록 조회
    fun findByOpen24HoursTrue(): List<HospitalAdditionalInfo>

    // 응급 진료 제공하는 병원 목록 조회
    fun findByEmergencyTreatmentTrue(): List<HospitalAdditionalInfo>

    // 남/여 전문의를 선택할 수 있는 병원 목록 조회
    fun findByMaleFemaleDoctorChoiceTrue(): List<HospitalAdditionalInfo>

    // 네트워크 병원 목록 조회
    fun findByNetworkHospitalTrue(): List<HospitalAdditionalInfo>

    // 무료 검진을 제공하는 병원 목록 조회
    fun findByFreeCheckupTrue(): List<HospitalAdditionalInfo>

    // 역세권에 위치한 병원 목록 조회
    fun findByNearSubwayTrue(): List<HospitalAdditionalInfo>

    // 연중무휴 운영하는 병원 목록 조회
    fun findByOpenAllYearTrue(): List<HospitalAdditionalInfo>

    // 일요일 및 공휴일에 진료하는 병원 목록 조회
    fun findByOpenOnSundayTrue(): List<HospitalAdditionalInfo>

    // 평일 야간 진료를 제공하는 병원 목록 조회
    fun findByNightShiftTrue(): List<HospitalAdditionalInfo>

    // 협진 시스템을 운영하는 병원 목록 조회
    fun findByCollaborativeCareTrue(): List<HospitalAdditionalInfo>

    // 점심시간 없이 운영하는 병원 목록 조회
    fun findByNoLunchBreakTrue(): List<HospitalAdditionalInfo>
}