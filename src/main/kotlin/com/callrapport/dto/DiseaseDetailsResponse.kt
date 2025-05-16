package com.callrapport.dto

import com.callrapport.model.disease.Disease

data class DiseaseDetailsResponse(
    val id: Long, // 질병 ID
    val name: String, // 질병 이름
    val category: String, // 단일 분류 이름
    val bodySystems: List<String>, // 신체계통 이름 목록
    val specialties: List<String> // 진료과 이름 목록
) {
    companion object {
        fun from(disease: Disease): DiseaseDetailsResponse {
            return DiseaseDetailsResponse(
                id = disease.id,
                name = disease.name,
                category = disease.diseaseCategory
                    .firstOrNull()?.category?.name ?: "",
                bodySystems = disease.diseaseBodySystem
                    .mapNotNull { it.bodySystem?.name },
                specialties = disease.diseaseSpecialties
                    .mapNotNull { it.specialty?.name }
            )
        }
    }
}
