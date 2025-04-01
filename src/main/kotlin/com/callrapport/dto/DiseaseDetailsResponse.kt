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
        // Disease 엔티티를 DTO로 변환
        fun from(entity: com.callrapport.model.disease.Disease): DiseaseDetailsResponse {
            return DiseaseDetailsResponse(
                id = entity.id, // 엔티티의 ID 사용
                name = entity.name, // 엔티티의 이름 사용
                bodySystem = entity.bodySystem, // 엔티티의 신체 계통 사용

                // 질병-증상 관계를 통해 연결된 증상 이름 리스트 추출
                symptoms = entity.diseaseSymptoms.mapNotNull { it.symptom?.name }, 

                // 질병-진료과 관계를 통해 연결된 진료과 이름 리스트 추출
                specialties = entity.diseaseSpecialties.mapNotNull { it.specialty?.name },

                // 질병-의사 관계를 통해 연결된 의사 ID 리스트 추출
                doctorIds = entity.diseaseDoctors.mapNotNull { it.doctor?.id }
            )
        }
    }
}
