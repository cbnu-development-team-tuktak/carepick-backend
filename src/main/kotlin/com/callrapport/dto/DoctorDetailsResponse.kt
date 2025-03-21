package com.callrapport.dto

// 엔티티 관련 import
import com.callrapport.model.doctor.Doctor

// DoctorDetailsResponse DTO 정의 (의사 상세 정보를 반환하는 응답 객체)
data class DoctorDetailsResponse(
    val id: String, // 의사 ID (Doctor 엔티티의 기본 키)
    val name: String, // 의사 이름
    val profileImage: String?, // 프로필 이미지
    val educationLicenses: List<String>, // 자격면허 목록
    val specialties: List<String>, // 진료과 목록
    val careers: List<String> // 의사 경력 목록
) {
    companion object {
        // Doctor 엔티티를 DoctorDetailsResponse DTO로 변환
        fun from(doctor: Doctor): DoctorDetailsResponse {
            return DoctorDetailsResponse(
                id = doctor.id, // 의사 ID
                name = doctor.name, // 의사 이름
                profileImage = doctor.profileImage, // 프로필 이미지 URL
                educationLicenses = doctor.educationLicenses.map { it.educationLicense.name }, // 자격면허 이름 목록
                specialties = doctor.specialties.map { it.specialty.name }, // 진료과 이름 목록
                careers = doctor.careers.map { it.career.name } // 경력 이름 목록
            )
        }
    }
}
