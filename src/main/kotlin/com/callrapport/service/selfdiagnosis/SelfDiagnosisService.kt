package com.callrapport.service.selfdiagnosis

// HTTP 통신 관련 import
import org.springframework.http.HttpEntity // HTTP 요청 본문 및 헤더를 함께 전달할 수 있는 클래스
import org.springframework.http.HttpHeaders // HTTP 요청 헤더 구성용 클래스
import org.springframework.http.MediaType // 콘텐츠 타입 정의용 클래스 

// Spring 서비스 관련 import
import org.springframework.stereotype.Service // 서비스 레이어 컴포넌트 지정용 어노테이션

// REST 클라이언트 관련 import
import org.springframework.web.client.RestTemplate // 외부 HTTP 요청을 수행하는 Spring REST 클라이언트

// Repository (저장소) 관련 import
import com.callrapport.repository.disease.DiseaseRepository // 질병 정보 조회 리포지토리
import com.callrapport.repository.disease.DiseaseSpecialtyRepository // 질병-진료과 관계 정보 조회 리포지토리

data class DiagnosisResult(
    val message: String, // 예측 결과 메시지 (예: "감기일 가능성이 있습니다")
    val suggestedSymptoms: List<String> = emptyList(), // 추천 증상 목록
    val suggestedSpecialties: List<String> = emptyList() // 추천 진료과 목록
)

@Service
class SelfDiagnosisService(
    // Repository: 질병 관련
    private val diseaseRepository: DiseaseRepository, // 질병 정보 조회 리포지토리
    private val diseaseSpecialtyRepository: DiseaseSpecialtyRepository, // 질병-진료과 관게 정보 조회 리포지토리
) {
    // 질병명 리스트를 받아 해당 질병들의 진료과 이름 목록을 반환
    private fun getSpecialtiesByDiseaseNames(
        diseaseNames: List<String> // 예측된 질병명 목록
    ): List<String> {
        // 질병명을 기반으로 질병 엔티티 리스트 조회
        val diseases = diseaseRepository.findByNameIn(diseaseNames)

        // 각 질병과 연결된 진료과 이름을 flatMap으로 펼치고 중복 제거
        return diseases
            .flatMap { disease ->
                diseaseSpecialtyRepository.findByDisease(disease) // 질병-진료과 관계 조회
                    .mapNotNull { it.specialty?.name } // 진료과명만 추출
            }
            .distinct() // 중복된 진료과 이름 제거 
    }

    // 자연어 기반 질병 예측
    fun diagnoseDisease(
        inputText: String?, // 사용자 입력 문장
        k: Int = 3 // Top-k 예측 개수 
    ): DiagnosisResult {
        // 입력 문장이 비어 있거나 null인 경우
        if (inputText.isNullOrBlank()) {
            return DiagnosisResult("입력된 문장이 비어 있습니다. 다시 입력해 주세요.") // 예외 메시지 반환
        }

        return try {
            // Flask 서버와 통신하기 위한 RestTemplate 객체 생성
            val restTemplate = RestTemplate()

            // Flask 서버의 질병 예측 모델 URL 구성
            val flaskUrl = "http://3.34.135.144:10000/disease?k=$k"

            // 요청 헤더 설정
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON // JSON 형식 명시
            }
            
            // 요청 본문에 보낼 JSON 데이터 구성
            val requestJson = mapOf("text" to inputText)

            // 요청 본문과 헤더를 함께 담은 HttpEntity 객체 생성
            val entity = HttpEntity(requestJson, headers)
            
            // Flask 서버에 POST 요청을 보내고 응답을 List 형태로 수신
            val response = restTemplate.postForEntity(
                flaskUrl, // 요청 URL
                entity, // 요청 본문 및 헤더 포함
                List::class.java // 응답 타입 (List 형태의 JSON)
            )
            
            // 응답 body를 List<Map> 형태로 안전하게 캐스팅(casting)
            val topk = response.body as? List<Map<*, *>> ?: emptyList()
            
            // 응답 리스트에서 'disease' 키에 해당한느 값을 추출하여 문자열 리스트로 변환
            val diseaseNames = topk.mapNotNull { it["disease"]?.toString() }

            // 예측된 질병명이 없는 경우
            if (diseaseNames.isEmpty()) {
                return DiagnosisResult("Flask 서버에서 예측된 질병이 없습니다.") // 예측 결과가 없다는 예외 메시지 반환
            }

            // 메시지 구성
            val message = buildString {
                appendLine("입력 문장: $inputText") // 입력 문장 출력
                appendLine("예측된 질병 Top-$k:") // 예측된 질병 Top-k 제목 출력
                
                // 예측 결과 리스트를 순회하며 질병명과 점수를 한 줄씩 출력
                topk.forEach {
                    val disease = it["disease"]?.toString() ?: "알 수 없음" // 질병명 추출
                    val score = it["score"]?.toString() ?: "?" // 점수 추출
                    appendLine("- $disease ($score)") // 질병명과 점수 결합
                }
            }
            
            // 예측된 질병명을 기반으로 진료과 이름 리스트 조회
            val specialties = getSpecialtiesByDiseaseNames(diseaseNames)

            // 예측 결과를 DiagnosisResult 형태로 반환
            DiagnosisResult(
                message = message, // 위에서 구성한 요약 메시지
                suggestedSymptoms = emptyList(), // 현재는 빈 리스트로 반환 (추후 삭제 예정)
                suggestedSpecialties = specialties // 추후 진료과 연동 시 대체 예정
            )

        // 예외가 발생했을 경우
        } catch (e: Exception) { 
            e.printStackTrace() // 예외 발생 시 스택 트레이스 출력
            // 예외 발생 시 사용자에게 오류 메시지를 포함한 결과 반환
            return DiagnosisResult("Flask 서버 호출 중 오류가 발생했습니다.")
        }
    }

    // 자연어 기반 진료과 예측
    fun diagnoseSpecialty(
        inputText: String?, // 사용자 입력 문장
        k: Int = 3 // Top-k 예측 개수
    ): DiagnosisResult {
        // 입력 문장이 비어 있거나 null인 경우
        if (inputText.isNullOrBlank()) {
            return DiagnosisResult("입력된 문장이 비어 있습니다. 다시 입력해 주세요.") // 에러 메시지 반환
        }

        return try {
            // Flask 서버와 통신하기 위한 RestTemplate 객체 생성
            val restTemplate = RestTemplate()

            // Flask 서버의 specialty 예측 URL 구성
            val flaskUrl = "http://3.34.135.144:10000/specialty?k=$k"

            // 요청 헤더 설정
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON // JSON 형식 명시
            }

            // 요청 본문에 보낼 JSON 데이터 구성
            val requestJson = mapOf("text" to inputText)

            // 요청 본문과 헤더를 함께 담은 HttpEntity 객체 생성
            val entity = HttpEntity(requestJson, headers)

            // Flask 서버에 POST 요청을 보내고 응답을 List 형태로 수신
            val response = restTemplate.postForEntity(
                flaskUrl, // 요청 URL
                entity, // 요청 본문 및 헤더 포함
                List::class.java // 응답 타입 (List 형태의 JSON)
            )

            // 응답 body를 List<Map> 형태로 안전하게 캐스팅(casting)
            val topk = response.body as? List<Map<*, *>> ?: emptyList()

            // 응답 리스트에서 'specialty' 키에 해당하는 값을 추출하여 문자열 리스트로 변환
            val specialtyNames = topk.mapNotNull { it["specialty"]?.toString() }

            // 예측된 진료과명이 없는 경우
            if (specialtyNames.isEmpty()) {
                return DiagnosisResult("Flask 서버에서 예측된 진료과가 없습니다.") // 예측 결과가 없다는 예외 메시지 반환
            }

            // 메시지 구성
            val message = buildString {
                appendLine("입력 문장: $inputText") // 입력 문장 출력
                appendLine("예측된 진료과 Top-$k") // 예측된 진료과 Top-k 제목 출력

                // 예측 결과 리스트를 순회하며 진료과명과 점수를 한 줄씩 출력
                topk.forEach {
                    val specialty = it["specialty"]?.toString() ?: "알 수 없음" // 진료과명 추출
                    val score = it["score"]?.toString() ?: "?" // 점수 추출
                    appendLine("- $specialty ($score)") // 진료과명과 점수 결합
                }
            }

            // 최종 결과 반환
            DiagnosisResult(
                message = message,
                suggestedSymptoms = emptyList(),
                suggestedSpecialties = specialtyNames
            )
        } catch (e: Exception) {
            e.printStackTrace() // 에외 발생 시 스택 트레이스 출력
            return DiagnosisResult("Flask 서버 호출 중 오류가 발생했습니다.")
        }
    }
}
