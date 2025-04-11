package com.callrapport.dto

// 엔티티 관련 import
import com.callrapport.model.disease.Symptom
import com.callrapport.model.disease.DiseaseSymptom

// SymptomDetailsResponse DTO 정의 (증상 상세 정보를 반환하는 응답 객체)
data class SymptomDetailsResponse(
    val id: Long, // 증상 ID
    val name: String, // 증상 이름
    val diseaseIds: List<Long> // 연결된 질병 ID 목록
) {
    companion object {
        fun from(symptom: Symptom): SymptomDetailsResponse {
            return SymptomDetailsResponse(
                id = symptom.id,
                name = symptom.name,
                diseaseIds = symptom.diseaseSymptoms
                    .mapNotNull { it.disease?.id }
            )
        }
    }
}
