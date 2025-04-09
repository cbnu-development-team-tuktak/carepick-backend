package com.callrapport.service.selfdiagnosis

// Model (엔티티) 관련 import
import com.callrapport.model.disease.Disease // 질병 정보를 담는 엔티티
import com.callrapport.model.disease.DiseaseSymptom // 질병-증상 간의 관계를 나타내는 엔티티
import com.callrapport.model.disease.Symptom // 증상 정보를 담는 엔티티

// Repository (저장소) 관련 import
import com.callrapport.repository.disease.DiseaseRepository // 질병 데이터를 처리하는 리포지토리
import com.callrapport.repository.disease.DiseaseSymptomRepository // 질병-증상 관계 데이터를 처리하는 리포지토리
import com.callrapport.repository.disease.SymptomRepository // 증상 데이터를 처리하는 리포지토리

// ChatGPT 연동 관련 import
import com.callrapport.component.chatgpt.ChatgptClient // ChatGPT 클라이언트 컴포넌트

// Spring 관련 import
import org.springframework.stereotype.Service // 해당 클래스를 서비스 빈으로 등록하기 위한 어노테이션

// 진단 결과 DTO
data class DiagnosisResult(
    val message: String, // 진단 메시지 또는 결과 설명
    val suggestedSymptoms: List<String> = emptyList(), // 추천된 증상 목록 (선택적으로 포함)
    val suggestedSpecialties: List<String> = emptyList() // 추천 진료과 목록 (선택적으로 포함)
)

@Service
class SelfDiagnosisService(
    private val diseaseRepository: DiseaseRepository, // 질병 정보를 조회하고 관리하는 리포지토리 
    private val symptomRepository: SymptomRepository, // 증상 정보를 조회하고 관리하는 리포지토리
    private val diseaseSymptomRepository: DiseaseSymptomRepository,  // 질병-증상 관계 데이터를 조회하는 어노테이션
    private val chatgptClient: ChatgptClient // ChatGPT를 통한 증상 추출 기능
) {
    companion object {
        // ChatGPT에게 증상 추출을 요청할 때 사용할 프롬프트 템플릿 (지시 + 규칙)
        private val SYMPTOM_EXTRACTION_PROMPT = """
            <지시>
            다음 문장에서 증상만 명사형으로 추출해서 [증상1, 증상2, ...] 형식으로 반환해줘.

            <규칙>
            - 리스트 형식으로 출력하여야 한다.
            - 증상은 명사형 표현으로 작성해야 한다.
            - 신체 반응 및 느낌만 증상에 포함한다.
            - 질병명은 증상 목록에서 제외한다.
            - 해부학적 부위명은 증상 목록에서 제외한다.

            <문장>
        """.trimIndent()
    }

    // 증상 기반 자가진단
    fun diagnoseBySymptoms(symptomNames: List<String>?): DiagnosisResult {
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

        // 질병별로 연결된 증상들을 저장할 맵 (Disease → Set<Symptom>)
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

        // 관련 진료과 정리 (리스트 형태로 저장 → DTO에 함께 전달)
        val suggestedSpecialties = topDiseases
            .flatMap { it.key.diseaseSpecialties.map { ds -> ds.specialty.name } } // DiseaseSpecialty → Specialty 이름
            .distinct()
            .sorted()

        // 진료과 문자열 메시지로 변환 (비어 있을 경우 '정보 없음')
        val relatedSpecialtyStr = suggestedSpecialties.joinToString(", ").ifEmpty { "정보 없음" }

        // 최종 메시지 구성
        val message = """
            입력한 증상 키워드: $symptomStr
            가장 관련 있는 질병:
            $diseaseDetails

            관련 진료과: $relatedSpecialtyStr
        """.trimIndent()

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

        // 진단 결과 반환 (메시지 + 추천 증상 + 추천 진료과)
        return DiagnosisResult(
            message = message, // 메시지
            suggestedSymptoms = suggestedSymptoms, // 추천 증상
            suggestedSpecialties = suggestedSpecialties // 추천 진료과
        )
    }

    // 질병 이름 기반 자가진단 (질병 이름 리스트를 입력받아 진료과 안내)
    fun diagnoseByDiseaseName(diseaseNames: List<String>?): DiagnosisResult {
        // 빈 입력 또는 null인 경우
        if (diseaseNames.isNullOrEmpty()) {
            return DiagnosisResult("입력된 질병명이 없습니다. 다시 시도해 주세요.")
        }

        // 중복 제거 및 공백 제거 후 정제된 이름 리스트 생성
        val cleanNames = diseaseNames.map { it.trim().replace("\"", "") }.distinct()

        // DB에서 해당 질병명을 가진 질병 목록 조회
        val matchedDiseases = diseaseRepository.findByNameIn(cleanNames)

        // 일치하는 질병이 하나도 없는 경우
        if (matchedDiseases.isEmpty()) {
            return DiagnosisResult("입력한 질병명과 일치하는 항목이 없습니다: ${cleanNames.joinToString(", ")}")
        }

        // 해당 질병에 연결된 진료과 정보 추출
        val specialties = matchedDiseases
            .flatMap { it.diseaseSpecialties.map { ds -> ds.specialty.name } }
            .distinct()
            .sorted()

        val matchedDiseaseNames = matchedDiseases.map { it.name }.sorted()

        // 응답 메시지 생성
        val message = """
            입력한 질병명: ${cleanNames.joinToString(", ")}
            매칭된 질병: ${matchedDiseaseNames.joinToString(", ")}
            관련 진료과: ${specialties.joinToString(", ").ifEmpty { "정보 없음" }}
        """.trimIndent()

        return DiagnosisResult(
            message = message,
            suggestedSymptoms = emptyList(),
            suggestedSpecialties = specialties
        )
    }

    fun diagnoseByNaturalLanguage(inputText: String?): DiagnosisResult {
        if (inputText.isNullOrBlank()) {
            return DiagnosisResult("입력된 문장이 비어 있습니다. 다시 입력해 주세요.")
        }
    
        val prompt = SYMPTOM_EXTRACTION_PROMPT + inputText
    
        return try {
            val response = chatgptClient.askQuestion(prompt).block()
            if (response.isNullOrBlank()) {
                return DiagnosisResult("ChatGPT로부터 응답을 받지 못했습니다.")
            }
    
            // 응답을 리스트로 파싱
            val symptomList = response
                .replace("[", "")
                .replace("]", "")
                .replace("\"", "")
                .replace("'", "")
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
    
            if (symptomList.isEmpty()) {
                return DiagnosisResult("문장에서 유효한 증상을 추출하지 못했습니다.")
            }
    
            // ChatGPT 응답 및 파싱 로그 메시지 (디버깅/검증용)
            val debugMessage = """
                ChatGPT 응답:
                $response
                
                파싱된 증상 리스트:
                ${symptomList.joinToString(", ")}
            """.trimIndent()
    
            // 증상 기반 진단 수행
            val diagnosis = diagnoseBySymptoms(symptomList)
    
            // ChatGPT 디버그 로그 포함해서 메시지 덮어쓰기
            return diagnosis.copy(message = debugMessage + "\n\n" + diagnosis.message)
    
        } catch (e: Exception) {
            e.printStackTrace()
            return DiagnosisResult("자연어 기반 자가진단 중 오류가 발생했습니다.")
        }
    }    
}
