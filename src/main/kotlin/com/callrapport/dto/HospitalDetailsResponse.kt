package com.callrapport.dto

// Model (엔티티) 관련 import
import com.callrapport.model.hospital.Hospital
import com.callrapport.model.hospital.HospitalAdditionalInfo // HospitalAdditionalInfo 임포트 추가
import com.callrapport.model.hospital.HospitalSpecialty
import com.callrapport.model.hospital.HospitalDoctor // HospitalDoctor 임포트 추가

// 병원 상세 정보 응답 DTO
data class HospitalDetailsResponse(
    val id: String, // 병원 ID
    val name: String, // 병원명
    val phoneNumber: String?, // 전화번호
    val homepage: String?, // 홈페이지 URL
    val address: String, // 병원 주소
    val operatingHours: String?, // 운영 시간
    val url: String, // 병원 정보 페이지 URL
    val specialties: List<String>?, // 병원의 진료과 목록 (nullable)
    val doctors: List<String>?, // 의사 목록 (nullable)
    val additionalInfo: HospitalAdditionalInfo? // 병원 추가 정보 (nullable)
) {
    companion object {
        fun from(hospital: Hospital): HospitalDetailsResponse {
            return HospitalDetailsResponse(
                id = hospital.id,
                name = hospital.name,
                phoneNumber = hospital.phoneNumber,
                homepage = hospital.homepage,
                address = hospital.address ?: "", // null 처리 안전하게 하기
                operatingHours = hospital.operatingHours,
                url = hospital.url ?: "", // null 처리 안전하게 하기
                specialties = if (hospital.specialties.isNotEmpty()) hospital.specialties.mapNotNull { it.specialty?.name } else null,
                doctors = if (hospital.doctors.isNotEmpty()) hospital.doctors.mapNotNull { it.doctor?.id } else null, // doctorId 대신 의사 ID 또는 다른 필드 사용
                additionalInfo = hospital.additionalInfo
            )
        }
    }
}
