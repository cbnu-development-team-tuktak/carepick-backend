package com.callrapport.dto

import com.callrapport.model.hospital.Hospital
import com.callrapport.model.hospital.HospitalAdditionalInfo
import com.callrapport.model.hospital.HospitalImage

data class HospitalDetailsResponse(
    val id: String, // 병원 ID
    val name: String, // 병원명
    val phoneNumber: String?, // 병원 전화번호 (선택적 필드, NULL 허용)
    val homepage: String?, // 병원 홈페이지 URL (선택적 필드, NULL 허용)
    val address: String, // 병원 주소
    val operatingHours: String?, // 병원 운영 시간 (선택적 필드, NULL 허용)
    val url: String, // 병원 정보 페이지 URL
    val specialties: List<String>?, // 병원의 진료과 목록 (선택적 필드, NULL 허용)
    val doctors: List<String>?, // 병원에 소속된 의사 목록 (선택적 필드, NULL 허용)
    val images: List<ImageResponse>?, // 병원과 연결된 이미지 정보 추가
    val additionalInfo: HospitalAdditionalInfo? // 병원 추가 정보 (선택적 필드, NULL 허용)
) {
    companion object {
        // Hospital 엔티티를 HospitalDetailsResponse DTO로 변환
        fun from(hospital: Hospital): HospitalDetailsResponse {
            return HospitalDetailsResponse(
                id = hospital.id,
                name = hospital.name,
                phoneNumber = hospital.phoneNumber,
                homepage = hospital.homepage,
                address = hospital.address ?: "",
                operatingHours = hospital.operatingHours,
                url = hospital.url ?: "",
                specialties = hospital.specialties.takeIf { it.isNotEmpty() }?.mapNotNull { it.specialty?.name },
                doctors = hospital.doctors.takeIf { it.isNotEmpty() }?.mapNotNull { it.doctor?.id },
                images = hospital.images.takeIf { it.isNotEmpty() }?.map { ImageResponse.from(it) }, // 병원 이미지 리스트 추가
                additionalInfo = hospital.additionalInfo
            )
        }
    }
}

// 병원 이미지 정보를 담을 DTO 추가
data class ImageResponse(
    val id: Long,
    val url: String,
    val alt: String
) {
    companion object {
        fun from(hospitalImage: HospitalImage): ImageResponse {
            return ImageResponse(
                id = hospitalImage.image.id ?: 0L,
                url = hospitalImage.image.url,
                alt = hospitalImage.image.alt
            )
        }
    }
}
