package com.callrapport.component.calculator

// DTO 및 모델 관련 import
import com.callrapport.dto.EducationLicenseDetailsResponse // EducationLicenseDetailsResponse: 의사의 교육 및 면허 정보를 담는 DTO
import com.callrapport.model.doctor.Doctor // Doctor: 의사 정보를 담는 도메인 모델
import com.callrapport.model.university.UniversityRank // UniversityRank: 대학 순위 정보를 담는 도메인 모델

// 리포지토리 관련 import
import com.callrapport.repository.doctor.DoctorRepository // 의사 데이터를 조회·저장하는 JPA 리포지토리

// Spring 관련 import 
import org.springframework.stereotype.Component // 해당 클래스를 Spring의 빈(Bean)으로 등록하는 어노테이션

data class ScoreDetails(
    val matchedKeyword: String, // 점수를 매기는 데 사용한 키워드 (예: "석사, "박사")
    val baseScore: Int, // 키워드에 해당하는 기본 점수
    val statusModifier: Double, // 상태(예: 졸업, 수료 등)에 따른 가중치
    val score: Double // 최종 계산된 점수 (기본 점수 x 가중치 )
)

// 학력/자격면허 유형
enum class EducationLicenseType(
    val label: String, // 분류 라벨 (예: 학위, 연수 등)
    val keywords: List<String>, // 타입에 해당하는 키워드 목록
    val scoreMap: Map<String, Int>, // 키워드별 점수 매핑
    val modifier: (String) -> Double // 상태(과정, 수료 등)에 따른 가중치 함수
) {
    DEGREE(
        "학위", // 분류 라벨
        listOf( // 학위와 관련된 키워드 목록
            "석박사", "석.박사", // 복합 키워드 (1순위)
            "박사", "석사", "학사", // 단일 학위 키워드 (2순위)
            "대학원", "대학" // 기관명 관련 키워드 (3순위)
        ),
        mapOf( // 키워드별 점수 매핑
            "석박사" to 5,
            "석.박사" to 5,
            "박사" to 3, 
            "석사" to 2,
            "학사" to 1,
            "대학원" to 2,
            "대학" to 1
        ),
        { status -> // 교육 상태에 따른 가중치를 반환
            when (status) {
                "과정" -> 0.5 
                "수료" -> 0.6
                "재학" -> 0.5
                "졸업" -> 1.0
                else -> 1.0 // 기타 상태는 기본 가중치 적용
            }
        }
    ),
    TRAINING(
        "수련", // 분류 라벨
        listOf("인턴", "수련", "수련의", "레지던트", "전공의"), // 수련과 관련된 키워드 목록
        mapOf( // 키워드별 점수 매핑
            "인턴" to 1, 
            "수련" to 2, 
            "수련의" to 2, 
            "레지던트" to 3, 
            "전공의" to 4
        ),
        { status -> 
            if (status == "과정") 0.5 // 과정 중이면 절반
            else 1.0 // 그 외 상태는 기본 가중치
        }
    ),
    POSITION(
        "직책", // 분류 라벨
        listOf("외래교수", "임상강사", "전임의", "조교수", "임상 부교수"), // 직책과 관련된 키워드 목록
        mapOf( // 키워드별 점수 매핑
            "외래교수" to 1, 
            "임상강사" to 2, 
            "전임의" to 3, 
            "조교수" to 4, 
            "임상 부교수" to 5
        ),
        { _ -> 1.0 } // 직책은 상태와 무관하게 항상 동일한 가중치
    )
}

@Component
class EducationLicenseScore (
    // 의사 관련 데이터를 처리하기 위한 의존성 주입
    private val doctorRepository: DoctorRepository
) {
    // 점수 작업 결과를 나타내는 응답 데이터 클래스
    data class UpdateResult(
        val success: Boolean, // 작업 성공 여부
        val message: String // 작업 결과 메시지 
    )

    // 주어진 텍스트에서 해당하는 EducationLicenseType을 판별
    private fun detectType(
        text: String // 학력/자격면허 설명 텍스트
    ): EducationLicenseType? {
        val lowered = text.lowercase() // 텍스트를 소문자로 변환하여 키워드 매칭을 용이하게 함

        // 해당 타입의 키워드 중 하나라도 포함되어 있는 경우, 해당 타입으로 반환
        return EducationLicenseType.values().firstOrNull { type ->
            type.keywords.any { keyword -> lowered.contains(keyword) } && 
            // 단, '학위' 타입일 때 '병원'이 포함되어 있는 경우, '학위'로 취급하지 않고 패스
            !(type == EducationLicenseType.DEGREE && lowered.contains("병원"))
        }
    }

    private fun calculateScore(
        text: String, // 학력/자격면허 설명 텍스트
        status: String, // 상태 정보 (예: "졸업", "과정", "수료" 등)
        type: EducationLicenseType // 판별된 학력/자격면허 유형
    ): ScoreDetails? {
        val lowered = text.lowercase() // 텍스트를 소문자로 변환하여 키워드 매칭을 쉽게 함

        // 해당 키워드의 타입 중 처음 발견된 것을 추출, 없으면 점수 0
        val keyword = type.keywords.firstOrNull { lowered.contains(it) } ?: return null

        // 키워드에 해당하는 기본 점수 조회, 없으면 점수 0
        val baseScore = type.scoreMap[keyword] ?: return null

        // 상태(졸업, 수료 등)에 따른 가중치 계산
        val modifier = type.modifier(status)

        // 최종 점수 계산 후 반환
        return ScoreDetails(
            matchedKeyword = keyword, 
            baseScore = baseScore, 
            statusModifier = modifier, 
            score = baseScore * modifier 
        )
    }

    fun calculateForAllDoctors(
        allDoctors: List<Doctor>, // 모든 의사 목록
        universityList: List<UniversityRank> // 대학 순위 정보 목록
    ): UpdateResult {
        // '대학교', '대', 'University'가 포함된 단어를 추출하기 위한 정규식 
        val universityRegex = Regex("""\b[\w가-힣]*(대학교|대|University)\b""", RegexOption.IGNORE_CASE)

        return try {
            allDoctors.forEach { doctor -> // 각 의사에 대해
                // 해당 의사의 교육/면허 이력을 순회하여 유효한 항목만 추출
                val entries = doctor.educationLicenses.mapNotNull { docEdu ->
                    val text = docEdu.educationLicense.name // 학력/자격면허 명칭 텍스트 추출
                    
                    // 정규식 패턴과 일치하지 않을 경우, null 반환
                    if (!universityRegex.containsMatchIn(text)) return@mapNotNull null

                    // universityList에서 텍스트와 매칭되는 대학을 찾아 해당 대학의 ID를 가져옴
                    val rank = universityList.firstOrNull { univ ->
                        // 대학 한국어 이름과 그 축약형(예: '서울대학교' → '서울대' 생성)
                        val krVariants = listOf(univ.krName, univ.krName.replace("대학교", "대"))
                        // 대학 영어 이름 리스트
                        val enVariants = listOf(univ.enName)
                        // 한글/영문 변형 중 하나라도 설명 텍스트에 포함되어 있으면 해당 대학으로 간주
                        (krVariants + enVariants).any { text.contains(it, ignoreCase = true) } 
                    }?.id ?: (universityList.size + 1) // 매칭되는 대학이 없을 경우 제일 끝 순위로 지정

                    // 상태 추출을 위해 텍스트를 소문자로 변환
                    val lowered = text.lowercase()

                    // 텍스트에 포함된 키워드를 기반으로 상태를 분류
                    val status = when {
                        "과정" in lowered -> "과정" 
                        "수료" in lowered -> "수료"
                        "재학" in lowered || "재직" in lowered -> "재학"
                        "졸업" in lowered -> "졸업"
                        else -> "기타" // 해당하는 키워드가 없을 경우 기본값
                    }

                    // 학력/자격면허 설명 텍스트로부터 유형을 감지
                    val type = detectType(text) ?: return@mapNotNull null

                    val scoreDetails = calculateScore(text, status, type) ?: return@mapNotNull null

                    // 대학 순위(rank)를 반영해 점수 계산
                    val weightedScore = scoreDetails.score * (1000.0 / rank)

                    // 계산된 점수를 해당 자격 항목에 저장
                    docEdu.educationLicense.score = weightedScore  
                }
                // 학력 점수를 포함한 의사 정보 재저장
                doctorRepository.save(doctor) 
            }
            
            // 모든 의사의 학력 점수가 정상적으로 업데이트되었다는 결과를 반환
            UpdateResult(
                success = true, // 업데이트 성공 여부 
                message = "모든 의사의 학력 점수가 성공적으로 업데이트되었습니다." // 성공 메시지
            )
        } catch (e: Exception) {
            // 업데이트 도중 예외가 발생한 경우 실패했다는 결과를 반환 
            UpdateResult(
                success = false, // 업데이트 실패 여부
                message = "업데이트 중 오류 발생: ${e.message}" // 예외 메시지 포함 실패 메시지
            )
        }
    }

}
