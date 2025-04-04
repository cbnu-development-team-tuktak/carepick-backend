package com.callrapport.service.selfdiagnosis

import com.callrapport.model.disease.Disease
import com.callrapport.model.disease.DiseaseSymptom
import com.callrapport.model.disease.Symptom
import com.callrapport.repository.disease.DiseaseSymptomRepository
import com.callrapport.repository.disease.SymptomRepository
import org.springframework.stereotype.Service

// 진단 결과 DTO
data class DiagnosisResult(
    val message: String,
    val suggestedSymptoms: List<String> = emptyList()
)

@Service
class SelfDiagnosisService(
    private val symptomRepository: SymptomRepository,
    private val diseaseSymptomRepository: DiseaseSymptomRepository
) {

    fun generateResponse(symptomNames: List<String>?): DiagnosisResult {
        if (symptomNames.isNullOrEmpty()) {
            return DiagnosisResult("증상이 감지되지 않았습니다. 다시 입력해 주세요.")
        }

        val uniqueNames = symptomNames.distinct()
        val matchedSymptoms = symptomRepository.findByNameIn(uniqueNames)

        if (matchedSymptoms.isEmpty()) {
            return DiagnosisResult("입력한 증상과 일치하는 항목이 없습니다. 다시 시도해 주세요.")
        }

        val diseaseSymptoms = diseaseSymptomRepository.findBySymptomIn(matchedSymptoms)
        if (diseaseSymptoms.isEmpty()) {
            return DiagnosisResult("입력한 증상들과 연결된 질병 정보를 찾을 수 없습니다.")
        }

        // Disease → Set<MatchedSymptom>
        val diseaseToSymptoms: MutableMap<Disease, MutableSet<Symptom>> = mutableMapOf()
        for (ds in diseaseSymptoms) {
            diseaseToSymptoms.computeIfAbsent(ds.disease) { mutableSetOf() }.add(ds.symptom)
        }

        // 질병별 입력 증상과 일치한 Symptom만 필터링
        val diseaseMatchInfo: Map<Disease, List<String>> = diseaseToSymptoms.mapValues { (_, symptomSet) ->
            symptomSet.map { it.name }.filter { it in uniqueNames }
        }

        val maxMatchCount = diseaseMatchInfo.values.maxOfOrNull { it.size } ?: 0
        val topDiseases = diseaseMatchInfo.filter { it.value.size == maxMatchCount }

        val symptomStr = uniqueNames.joinToString(", ")
        val diseaseDetails = topDiseases.entries.joinToString("\n") { (disease, matchedNames) ->
            "- ${disease.name} (${matchedNames.size}개 일치): ${matchedNames.joinToString(", ")}"
        }

        // 함께 나타날 수 있는 증상 추천 로직
        val relatedDiseases = diseaseMatchInfo.keys
        val relatedDiseaseSymptoms = diseaseSymptomRepository.findByDiseaseIn(relatedDiseases.toList())

        val suggestedSymptoms = relatedDiseaseSymptoms.map { it.symptom.name }
            .filter { it !in uniqueNames }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .map { it.first }
            .take(5)

        val message = """
            입력한 증상 키워드: $symptomStr
            가장 관련 있는 질병:
            $diseaseDetails
        """.trimIndent()

        return DiagnosisResult(message, suggestedSymptoms)
    }
}
