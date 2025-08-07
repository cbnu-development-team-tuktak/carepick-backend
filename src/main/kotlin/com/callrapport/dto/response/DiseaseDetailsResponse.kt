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
                id = disease.id, // 질병 ID 매핑 
                name = disease.name, // 질병 이름 매핑
                category = disease.diseaseCategory
                    .firstOrNull()?.category?.name ?: "", // 첫 번째 질병 분류의 이름 
                bodySystems = disease.diseaseBodySystem
                    .mapNotNull { it.bodySystem?.name }, // 신체계통 이름 리스트로 매핑
                specialties = disease.diseaseSpecialties
                    .mapNotNull { it.specialty?.name } // 진료과 이름 리스트로 매핑 
            )
        }
    }
}
