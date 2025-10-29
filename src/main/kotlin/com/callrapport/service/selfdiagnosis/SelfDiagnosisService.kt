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

// 설정 및 DTO 관련 import
import com.callrapport.config.ChatgptApiProperties // ChatGPT API 설정 프로퍼티
import com.callrapport.dto.request.ChatgptRequest // ChatGPT 요청 DTO
import com.callrapport.dto.request.Message // ChatGPT 메시지 DTO
import com.callrapport.dto.response.ChatgptResponse // ChatGPT 응답 DTO

data class DiagnosisResult(
    val message: String, // 예측 결과 메시지 (예: "감기일 가능성이 있습니다")
    val suggestedSymptoms: List<String> = emptyList(), // 추천 증상 목록
    val suggestedSpecialties: List<String> = emptyList() // 추천 진료과 목록
)

@Service
class SelfDiagnosisService(
    // Repository: 질병 관련
    private val diseaseRepository: DiseaseRepository, // 질병 정보 조회 리포지토리
    private val diseaseSpecialtyRepository: DiseaseSpecialtyRepository, // 질병-진료과 관계 정보 조회 리포지토리
    private val chatgptApiProperties: ChatgptApiProperties // ChatGPT API 키 설정 주입
) {
    // ChatGPT에 증상을 보내 질병 목록을 받아오는 내부 함수
    private fun getDiseaseNamesFromGpt(inputText: String): List<String> {
        val restTemplate = RestTemplate()
        val chatgptUrl = "https://api.openai.com/v1/chat/completions"

        val headers = HttpHeaders().apply {
            setBearerAuth(chatgptApiProperties.apiKey)
            contentType = MediaType.APPLICATION_JSON
        }

        // 질병명만 JSON 배열로 요청하는 프롬프트
        val prompt = """
            사용자의 증상은 '${inputText}' 입니다.
            가장 관련성 높은 질병명 3개를 다른 설명 없이 JSON 문자열 배열 형식으로만 응답해주세요.
            예시: ["감기", "독감", "편도염"]
        """.trimIndent()
        
        val requestBody = ChatgptRequest(
            model = "gpt-4o",
            messages = listOf(Message(role = "user", content = prompt))
        )

        val entity = HttpEntity(requestBody, headers)

        return try {
            val response = restTemplate.postForEntity(chatgptUrl, entity, ChatgptResponse::class.java)
            val jsonString = response.body?.choices?.firstOrNull()?.message?.content ?: "[]"

            // JSON 문자열을 List<String>으로 파싱
            val objectMapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
            objectMapper.readValue(jsonString, objectMapper.typeFactory.constructCollectionType(List::class.java, String::class.java))
        
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // 오류 발생 시 빈 리스트 반환
        }
    }

    // ChatGPT에 질병 목록을 보내 진료과 목록을 받아오는 내부 함수
    private fun getSpecialtiesFromGpt(diseaseNames: List<String>): List<String> {
        // 질병 목록이 비어있으면 API 호출 없이 빈 리스트 반환
        if (diseaseNames.isEmpty()) {
            return emptyList()
        }

        val restTemplate = RestTemplate()
        val chatgptUrl = "https://api.openai.com/v1/chat/completions"

        val headers = HttpHeaders().apply {
            setBearerAuth(chatgptApiProperties.apiKey)
            contentType = MediaType.APPLICATION_JSON
        }

        // 진료과만 JSON 배열로 요청하는 프롬프트
        val prompt = """
            다음 질병 목록과 관련된 모든 진료과를 중복 없이 JSON 문자열 배열 형식으로만 응답해주세요: ${diseaseNames.joinToString(", ")}.
            예시: ["이비인후과", "내과", "가정의학과"]
        """.trimIndent()

        val requestBody = ChatgptRequest(
            model = "gpt-4o",
            messages = listOf(Message(role = "user", content = prompt))
        )

        val entity = HttpEntity(requestBody, headers)

        return try {
            val response = restTemplate.postForEntity(chatgptUrl, entity, ChatgptResponse::class.java)
            val jsonString = response.body?.choices?.firstOrNull()?.message?.content ?: "[]"

            // JSON 문자열을 List<String>으로 파싱
            val objectMapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
            objectMapper.readValue(jsonString, objectMapper.typeFactory.constructCollectionType(List::class.java, String::class.java))

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // 오류 발생 시 빈 리스트 반환
        }
    }

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
            val flaskUrl = "http://43.201.15.104:10000/disease?k=$k"
            val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
            val requestJson = mapOf("text" to inputText)
            val entity = HttpEntity(requestJson, headers)
            val response = restTemplate.postForEntity(flaskUrl, entity, List::class.java)
            val topk = response.body as? List<Map<*, *>> ?: emptyList()

            // Flask 응답 데이터 유효성 검사
            val isResponseValid = topk.all { it.containsKey("disease") && it.containsKey("score") }
            if (!isResponseValid) {
                return DiagnosisResult("일시적인 오류로 분석에 실패했습니다. 잠시 후 다시 시도해 주세요.")
            }

            val topScore = topk.firstOrNull()?.get("score")?.toString()?.toDoubleOrNull() ?: 0.0

            // --- 수정된 부분: ChatGPT 로직 ---
            // 점수가 0.4 미만일 경우 ChatGPT로 재진단 (1차 시도)
            if (topScore < 0.4) {
                val diseaseNamesFromGpt = getDiseaseNamesFromGpt(inputText)
                val specialtiesFromGpt = getSpecialtiesFromGpt(diseaseNamesFromGpt)
                
                // ChatGPT가 성공적으로 답변을 생성한 경우에만, ChatGPT 결과를 반환
                if (diseaseNamesFromGpt.isNotEmpty() && specialtiesFromGpt.isNotEmpty()) {
                    val message = buildString {
                        appendLine("예측된 질병 (ChatGPT 분석):")
                        diseaseNamesFromGpt.forEach { disease ->
                            appendLine("- $disease")
                        }
                    }

                    return DiagnosisResult(
                        message = message,
                        suggestedSpecialties = specialtiesFromGpt
                    )
                }
                // 💡 만약 ChatGPT가 실패하면 (위 if문을 타지 않으면)
                // 'return'하지 않고 if 블록을 빠져나가,
                // 아래의 '자체 모델 예측 로직'을 실행하게 됨 (Fallback)
            }
            
            // --- 자체 모델 예측 성공 시 로직 (Fallback 또는 topScore >= 0.4) ---
            // 1. topScore >= 0.4 인 경우
            // 2. topScore < 0.4 였지만 ChatGPT가 실패한 경우
            // 위 두 가지 경우 모두 이 로직을 실행함
            val diseaseNames = topk.mapNotNull { it["disease"]?.toString() }
            if (diseaseNames.isEmpty()) {
                return DiagnosisResult("Flask 서버에서 예측된 질병이 없습니다.")
            }

            // 1. DB에서 예측된 질병 정보와 연결된 진료과 정보를 조회
            val existingDiseases = diseaseRepository.findByNameIn(diseaseNames)
            val specialtiesMap = existingDiseases.associateWith { disease ->
                diseaseSpecialtyRepository.findByDisease(disease).mapNotNull { it.specialty?.name }
            }

            // 2. DB에서 찾은 진료과 목록을 추출
            val specialtiesFromDb = specialtiesMap.values.flatten()

            // 3. DB에 없거나, DB에 있지만 진료과 정보가 없는 질병 목록을 구성
            val dbDiseaseNames = existingDiseases.map { it.name }
            val newDiseaseNames = diseaseNames.filterNot { it in dbDiseaseNames }
            val diseasesWithNoSpecialties = specialtiesMap.filterValues { it.isEmpty() }.keys.map { it.name }
            val diseasesToAskGpt = (newDiseaseNames + diseasesWithNoSpecialties).distinct()

            // 4. 필요시 ChatGPT를 통해 나머지 진료과를 조회
            val specialtiesFromGpt = if (diseasesToAskGpt.isNotEmpty()) {
                getSpecialtiesFromGpt(diseasesToAskGpt)
            } else {
                emptyList()
            }
            
            // 5. 두 진료과 목록을 합치고 중복을 제거
            val allSpecialties = (specialtiesFromDb + specialtiesFromGpt).distinct()
            
            // 메시지 구성
            val message = buildString {
                appendLine("예측된 질병 Top-$k:")
                topk.forEach {
                    val disease = it["disease"]!!.toString()
                    val score = it["score"]!!.toString()
                    appendLine("- $disease ($score)")
                }
            }
            
            // 최종 결과 반환
            return DiagnosisResult(
                message = message,
                suggestedSymptoms = emptyList(),
                suggestedSpecialties = allSpecialties
            )

        // 예외가 발생했을 경우
        } catch (e: Exception) { 
            e.printStackTrace()
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
            val flaskUrl = "http://43.201.15.104:10000/specialty?k=$k"

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

            // Flask 응답 데이터 유효성 검사
            val isFlaskResponseValid = topk.all { it.containsKey("specialty") && it.containsKey("score") }
            if (!isFlaskResponseValid) {
                return DiagnosisResult("일시적인 오류로 분석에 실패했습니다. 잠시 후 다시 시도해 주세요.")
            }

            // 가장 높은 점수를 확인
            val topScore = topk.firstOrNull()?.get("score")?.toString()?.toDoubleOrNull() ?: 0.0

            // 점수가 0.4 미만일 경우 ChatGPT로 재진단
            if (topScore < 0.4) {
                // ChatGPT에 직접 증상을 설명하고 진료과를 추천받음
                val specialtiesFromGpt = getSpecialtiesFromGpt(listOf(inputText)) // 기존 함수 재활용
                
                if (specialtiesFromGpt.isEmpty()) {
                    return DiagnosisResult("일시적인 오류로 자세한 분석에 실패했습니다. 잠시 후 다시 시도해 주세요.")
                }

                // ChatGPT 예측 결과로 메시지 구성
                val message = buildString {
                    appendLine("추천 진료과 (ChatGPT 분석):")
                    specialtiesFromGpt.forEach { specialty ->
                        appendLine("- $specialty")
                    }
                }

                return DiagnosisResult(
                    message = message,
                    suggestedSpecialties = specialtiesFromGpt
                )
            }

            // 점수가 0.4 이상일 경우, 기존 로직 수행
            val specialtyNames = topk.map { it["specialty"]!!.toString() }

            if (specialtyNames.isEmpty()) {
                return DiagnosisResult("Flask 서버에서 예측된 진료과가 없습니다.")
            }

            // 메시지 구성
            val message = buildString {
                appendLine("예측된 진료과 Top-$k")
                topk.forEach {
                    val specialty = it["specialty"]!!.toString()
                    val score = it["score"]!!.toString()
                    appendLine("- $specialty ($score)")
                }
            }

            // 최종 결과 반환
            DiagnosisResult(
                message = message,
                suggestedSymptoms = emptyList(),
                suggestedSpecialties = specialtyNames
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return DiagnosisResult("Flask 서버 호출 중 오류가 발생했습니다.")
        }
    }

    // 테스트용: ChatGPT 질병 예측 직접 호출
    fun diagnoseDiseaseWithGpt(inputText: String): DiagnosisResult {
        val diseaseNamesFromGpt = getDiseaseNamesFromGpt(inputText)
        val specialtiesFromGpt = getSpecialtiesFromGpt(diseaseNamesFromGpt)

        if (diseaseNamesFromGpt.isEmpty() || specialtiesFromGpt.isEmpty()) {
            return DiagnosisResult("ChatGPT API 호출에 실패했거나 유효한 응답을 받지 못했습니다.")
        }

        val message = buildString {
            appendLine("예측된 질병 (ChatGPT 분석):")
            diseaseNamesFromGpt.forEach { disease ->
                appendLine("- $disease")
            }
        }

        return DiagnosisResult(
            message = message,
            suggestedSpecialties = specialtiesFromGpt
        )
    }

    // 테스트용: ChatGPT 진료과 예측 직접 호출
    fun diagnoseSpecialtyWithGpt(inputText: String): DiagnosisResult {
        val specialtiesFromGpt = getSpecialtiesFromGpt(listOf(inputText))

        if (specialtiesFromGpt.isEmpty()) {
            return DiagnosisResult("ChatGPT API 호출에 실패했거나 유효한 응답을 받지 못했습니다.")
        }

        val message = buildString {
            appendLine("추천 진료과 (ChatGPT 분석):")
            specialtiesFromGpt.forEach { specialty ->
                appendLine("- $specialty")
            }
        }

        return DiagnosisResult(
            message = message,
            suggestedSpecialties = specialtiesFromGpt
        )
    }
}

