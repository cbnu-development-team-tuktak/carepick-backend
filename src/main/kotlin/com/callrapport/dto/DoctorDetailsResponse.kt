package com.callrapport.dto

// 엔티티 관련 import
import com.callrapport.model.doctor.Doctor
import com.callrapport.model.hospital.HospitalDoctor // 병원 정보를 포함하기 위한 import

// DoctorDetailsResponse DTO 정의 (의사 상세 정보를 반환하는 응답 객체)
data class DoctorDetailsResponse(
    val id: String, // 의사 ID (Doctor 엔티티의 기본 키)
    val name: String, // 의사 이름
    val profileImage: String?, // 프로필 이미지
    val educationLicenses: List<String>, // 자격면허 목록
    val specialties: List<String>, // 진료과 목록
    val careers: List<String>, // 의사 경력 목록

    val hospitalId: String?, // 소속 병원 ID (Hospital 엔티티의 기본 키, nullable)
    val hospitalName: String? // 소속 병원 이름 (nullable)
) {
    companion object {
        // Doctor 엔티티와 HospitalDoctor 엔티티를 DoctorDetailsResponse DTO로 변환
        fun from(doctor: Doctor, hospitalDoctor: HospitalDoctor?): DoctorDetailsResponse {
            return DoctorDetailsResponse(
                id = doctor.id, // 의사 ID
                name = doctor.name, // 의사 이름
                profileImage = doctor.profileImage, // 프로필 이미지 URL
                educationLicenses = doctor.educationLicenses.map { it.educationLicense.name }, // 자격면허 이름 목록
                specialties = doctor.specialties.map { it.specialty.name }, // 진료과 이름 목록
                careers = doctor.careers.map { it.career.name }, // 경력 이름 목록

                hospitalId = hospitalDoctor?.hospital?.id, // 병원 ID (nullable)
                hospitalName = hospitalDoctor?.hospital?.name // 병원 이름 (nullable)
            )
        }
    }
}
