package com.callrapport.dto

// 엔티티 관련 import
import com.callrapport.model.hospital.Hospital // Hospital: 병원 정보를 저장하는 엔티티
import com.callrapport.model.hospital.HospitalAdditionalInfo // HospitalAdditionalInfo: 병원의 추가 정보를 저장하는 엔티티
import com.callrapport.model.hospital.HospitalSpecialty // HospitalSpecialty: 병원과 진료과 간의 관계를 저장하는 엔티티
import com.callrapport.model.hospital.HospitalDoctor // HospitalDoctor: 병원과 의사 간의 관계를 저장하는 엔티티

// HospitalDetailsResponse DTO 정의
data class HospitalDetailsResponse(
    val id: String, // 병원 ID
    val name: String, // 병원명
    val phoneNumber: String?, // 병원 전화번호 (선택적 필드, NULL 허용)
    val homepage: String?, // 병원 홈페이지 URL (선택적 필드, NULl 허용)
    val address: String, // 병원 주소
    val operatingHours: String?, // 병원 운영 시간 (선택적 필드, NULL 허용)
    val url: String, // 병원 정보 페이지 URL
    val specialties: List<String>?, // 병원의 진료과 목록 (선택적 필드, NULL 허용)
    val doctors: List<String>?, // 병원에 소속된 의사 목록 (선택적 필드, NULL 허용)
    val additionalInfo: HospitalAdditionalInfo? // 병원 추가 정보 (선택적 필드, NULL 허용)
) {
    companion object {
        // Hospital 엔티티를 HospitalDetailsResponse DTO로 변환
        fun from(hospital: Hospital): HospitalDetailsResponse {
            return HospitalDetailsResponse(
                id = hospital.id, // Hospital 엔티티의 ID
                name = hospital.name, // Hospital 엔티티의 이름
                phoneNumber = hospital.phoneNumber, // 병원 전화번호
                homepage = hospital.homepage, // 병원 홈페이지 URL
                address = hospital.address ?: "", // 병원 주소
                operatingHours = hospital.operatingHours, // 병원 운영 시간
                url = hospital.url ?: "", // 병원 정보 페이지 URL
                specialties = if (hospital.specialties.isNotEmpty()) 
                    hospital.specialties.mapNotNull { it.specialty?.name } 
                else null, // 병원의 진료과 목록 반환
                doctors = if (hospital.doctors.isNotEmpty()) 
                    hospital.doctors.mapNotNull { it.doctor?.id } 
                else null, // 병원에 소속된 의사 목록 반환
                additionalInfo = hospital.additionalInfo // 병원의 추가 정보
            )
        }
    }
    
}
