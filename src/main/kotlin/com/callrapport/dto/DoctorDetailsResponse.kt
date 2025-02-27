package com.callrapport.dto

// Model (엔티티) 관련 import
import com.callrapport.model.doctor.Doctor // Doctor 엔티티: 의사 정보를 저장하는 엔티티 (의사 ID, 이름, 프로필 이미지, 진료과 등)

data class DoctorDetailsResponse(
    val id: String, // 의사 ID
    val name: String, // 의사 이름
    val profileImage: String?, // 프로필 이미지 URL
    val educationLicenses: List<String>, // 의사의 학력 및 자격면허 목록
    val hospitalId: String?, // 병원 ID
    val specialty: String? // 의사의 진료과 
) {
    companion object {
        fun from(
            doctor: Doctor // 변환할 Doctor 엔티티 객체
        ): DoctorDetailsResponse {
            return DoctorDetailsResponse(
                id = doctor.id, 
                name = doctor.name,
                profileImage = doctor.profileImage,
                educationLicenses = doctor.educationLicenses.mapNotNull { it.educationLicense }, // educationLicenses 리스트를 문자열 리스트로 변환
                hospitalId = doctor.hospitalId, 
                specialty = doctor.specialty?.name ?: "미등록" // 진료과가 없을 경우 미등록을 반환
            )
        }
    }
}
