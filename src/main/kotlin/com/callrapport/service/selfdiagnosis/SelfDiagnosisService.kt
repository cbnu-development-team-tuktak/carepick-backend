package com.callrapport.service.selfdiagnosis

// Model (엔티티) 관련 import
import com.callrapport.model.disease.Disease // 질병 정보를 담는 엔티티
import com.callrapport.model.disease.DiseaseSymptom // 질병-증상 간의 관계를 나타내는 엔티티
import com.callrapport.model.disease.Symptom // 증상 정보를 담는 엔티티

// Repository (저장소) 관련 import
import com.callrapport.repository.disease.DiseaseSymptomRepository // 질병-증상 관계 데이터를 처리하는 리포지토리
import com.callrapport.repository.disease.SymptomRepository // 증상 데이터를 처리하는 리포지토리

// Spring 관련 import
import org.springframework.stereotype.Service // 해당 클래스를 서비스 빈으로 등록하기 위한 어노테이션

// 진단 결과 DTO
data class DiagnosisResult(
    val message: String, // 진단 메시지 또는 결과 설명
    val suggestedSymptoms: List<String> = emptyList() // 추천된 증상 목록 (선택적으로 포함)
)

@Service
class SelfDiagnosisService(
    private val symptomRepository: SymptomRepository, // 증상 정보를 조회하고 관리하는 리포지토리
    private val diseaseSymptomRepository: DiseaseSymptomRepository // 질병-증상 관계 데이터를 조회하는 어노테이션
) {

    fun generateResponse(symptomNames: List<String>?): DiagnosisResult {
        // 입력한 증상 리스트가 null이거나 비어 있는 경우
        if (symptomNames.isNullOrEmpty()) {
            return DiagnosisResult("증상이 감지되지 않았습니다. 다시 입력해 주세요.")
        }

        // 중복된 증상 이름을 제거하여 고유한 증상 이름 리스트 생성
        val uniqueNames = symptomNames.distinct()

        // 데이터베이스에서 입력된 증상 이름과 일치하는 Symptoms 엔티티 목록 조회
        val matchedSymptoms = symptomRepository.findByNameIn(uniqueNames)

        // 일치하는 증상이 없는 경우
        if (matchedSymptoms.isEmpty()) {
            return DiagnosisResult("입력한 증상과 일치하는 항목이 없습니다. 다시 시도해 주세요.")
        }

        // 해당 증상들과 연결된 질병-증상 관계 데이터를 조회
        val diseaseSymptoms = diseaseSymptomRepository.findBySymptomIn(matchedSymptoms)

        // 연결된 질병 정보가 없는 경우
        if (diseaseSymptoms.isEmpty()) {
            return DiagnosisResult("입력한 증상들과 연결된 질병 정보를 찾을 수 없습니다.")
        }
        
        // 질별별로 연결된 증상들을 저장할 맵 (Disease → Set<Symptom>)
        val diseaseToSymptoms: MutableMap<Disease, MutableSet<Symptom>> = mutableMapOf()
        for (ds in diseaseSymptoms) {
            diseaseToSymptoms.computeIfAbsent(ds.disease) { mutableSetOf() }.add(ds.symptom)
        }

        // 조회된 질병-증상 관계 데이터를 통해 질병별 증상 목록 구성
        val diseaseMatchInfo: Map<Disease, List<String>> = diseaseToSymptoms.mapValues { (_, symptomSet) ->
            // 질병 키가 없으면 새로 추가하고, 증상을 해당 질병의 Set에 추가
            symptomSet.map { it.name }.filter { it in uniqueNames }
        }

        // 가장 많은 증상이 일치하는 질병의 일치 개수 추출
        val maxMatchCount = diseaseMatchInfo.values.maxOfOrNull { it.size } ?: 0
        
        // 최대 일치 개수를 가진 질병만 추출 (동률 가능성 있음)
        val topDiseases = diseaseMatchInfo.filter { it.value.size == maxMatchCount }

        // 입력한 증상들을 문자열로 연결 (진단 메시지용)
        val symptomStr = uniqueNames.joinToString(", ")

        // 최종 진단 메시지에 사용할 질병 설명 문자열 구성
        val diseaseDetails = topDiseases.entries.joinToString("\n") { (disease, matchedNames) ->
            "- ${disease.name} (${matchedNames.size}개 일치): ${matchedNames.joinToString(", ")}"
        }

        // 일치한 증상 목록 (Disease 객체들) 추출
        val relatedDiseases = diseaseMatchInfo.keys

        // 해당 질병들과 연결된 모든 질병-증상 관계를 조회
        val relatedDiseaseSymptoms = diseaseSymptomRepository.findByDiseaseIn(relatedDiseases.toList())

        // 추천 증상 생성
        val suggestedSymptoms = relatedDiseaseSymptoms
            .map { it.symptom.name } // 증상 이름 추출
            .filter { it !in uniqueNames } // 사용자가 입력한 증상은 제외
            .groupingBy { it } // 증상 이름을 그룹화
            .eachCount() // 각 증상이 몇 번 나왔는지 계산
            .toList() // (이름, 개수) 쌍 리스트로 변환
            .sortedByDescending { it.second } // 등장 횟수 기준 내림차순 정렬
            .map { it.first } // 이름만 추출
            .take(5) // 상위 5개만 추천

        // 최종 진단 메시지 구성
        val message = """
            입력한 증상 키워드: $symptomStr
            가장 관련 있는 질병:
            $diseaseDetails
        """.trimIndent()

        // 진단 결과 반환 (메시지 + 추천 증상)
        return DiagnosisResult(message, suggestedSymptoms)
    }
}
