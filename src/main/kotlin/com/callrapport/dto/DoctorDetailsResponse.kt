package com.callrapport.dto

// Model (엔티티) 관련 import
import com.callrapport.model.doctor.Doctor

data class DoctorDetailsResponse(
    val id: String, // 의사 ID
    val name: String, // 의사 이름
    val specialties: List<String> // ✅ 의사의 진료과 목록 (N:M 관계 반영)
) {
    companion object {
        fun from(doctor: Doctor): DoctorDetailsResponse {
            return DoctorDetailsResponse(
                id = doctor.id, 
                name = doctor.name,
                specialties = doctor.specialties.map { it.specialty.name } // ✅ 여러 개의 진료과를 리스트로 변환
            )
        }
    }
}
