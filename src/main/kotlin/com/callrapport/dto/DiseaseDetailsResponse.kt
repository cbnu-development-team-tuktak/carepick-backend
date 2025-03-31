package com.callrapport.dto

// 응답용 DTO
data class DiseaseDetailsResponse(
    val id: Long, // 질병 ID
    val name: String, // 질병명
    val bodySystem: String, // 신체 계통
    val symptoms: List<String>, // 연결된 증상 이름 목록
    val specialties: List<String>, // 연결된 진료과 이름 목록
    val doctorIds: List<String> // 연결된 의사 ID 목록
) {
    companion object {
        fun from(entity: com.callrapport.model.disease.Disease): DiseaseDetailsResponse {
            return DiseaseDetailsResponse(
                id = entity.id,
                name = entity.name,
                bodySystem = entity.bodySystem,
                symptoms = entity.diseaseSymptoms.mapNotNull { it.symptom?.name },
                specialties = entity.diseaseSpecialties.mapNotNull { it.specialty?.name },
                doctorIds = entity.diseaseDoctors.mapNotNull { it.doctor?.id }
            )
        }
    }
}
