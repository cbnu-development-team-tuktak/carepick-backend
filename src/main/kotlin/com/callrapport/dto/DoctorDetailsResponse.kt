package com.callrapport.dto

// 엔티티 관련 import
import com.callrapport.model.doctor.Doctor // Doctor: 의사 정보를 저장하는 엔티티

// DoctorDetailsResponse DTO 정의 (의사 상세 정보를 반환하는 응답 객체)
data class DoctorDetailsResponse(
    val id: String, // 의사 ID (Doctor 엔티티의 기본 키)
    val name: String, // 의사 이름
    val specialties: List<String> // 의사의 진료과 목록
) {
    companion object {
        // Doctor 엔티티를 DoctorDetailsResponse DTO로 변경
        fun from(doctor: Doctor): DoctorDetailsResponse {
            return DoctorDetailsResponse(
                id = doctor.id, // Doctor 엔티티의 ID
                name = doctor.name, // Doctor 엔티티의 이름
                specialties = doctor.specialties.map { it.specialty.name } // DoctorSpecialty 엔티티를 통해 Specialty의 이름 리스트로 변환
            )
        }
    }
}
