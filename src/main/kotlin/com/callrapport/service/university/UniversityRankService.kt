package com.callrapport.service.university

// 컴포넌트 관련 import
import com.callrapport.component.calculator.EducationLicenseScore //  학력·자격면허 점수 계산 담당 컴포넌트

// Model (엔티티) 관련 import
import com.callrapport.model.university.UniversityRank // 대학 랭킹 정보를 나타내는 엔티티

// Repository (저장소) 관련 import
import com.callrapport.repository.university.UniversityRankRepository // 대학 랭킹 정보를 조회하는 저장소
import com.callrapport.repository.doctor.DoctorRepository // 의사 엔티티를 조회·저장하는 저장소

// DTO 관련 import
import com.callrapport.dto.EducationLicenseDetailsResponse // 학력·자격면허 상세 응답 DTO

// Spring 및 JPA 관련 import
import org.springframework.data.domain.Page // 페이지 단위 조회 결과를 표현하는 JPA 객체
import org.springframework.data.domain.Pageable // 페이지 요청 정보를 담는 JPA 객체
import org.springframework.stereotype.Service // 해당 클래스를 Spring의 서비스 컴포넌트로 등록하는 어노테이션

// 기타 import
import com.callrapport.component.calculator.EducationLicenseScore.UpdateResult // 점수 업데이트 결과 

@Service
class UniversityRankService(
    private val universityRankRepository: UniversityRankRepository, // 의대 순위 저장소 의존성 주입
    private val doctorRepository: DoctorRepository, // 의사 저장소 의존성 주입
    private val educationLicenseScore: EducationLicenseScore // 학력·자격면허 점수 계산 의존성 주입
) {
    // 모든 대학 랭킹 정보를 페이지네이션으로 조회
    fun getAllUniversityRanks(
        pageable: Pageable // 페이지네이션 정보를 포함한 객체
    ): Page<UniversityRank> {
        return universityRankRepository.findAll(pageable)
    }

    // 입력한 한글 키워드를 포함하는 대학명을 페이지네이션으로 검색
    fun searchByKrName(
        keyword: String, // 검색할 한글 대학명 키워드 
        pageable: Pageable // 페이지네이션 정보를 포함한 객체
    ): Page<UniversityRank> {
        return universityRankRepository.findByKrNameContaining(keyword, pageable)
    }

    // 입력한 영어 키워드를 포함하는 대학명을 페이지네이션으로 검색
    fun searchByEnName(
        keyword: String, // 검색할 영어 대학명 키워드
        pageable: Pageable // 페이지네이션 정보를 포함한 객체
    ): Page<UniversityRank> {
        return universityRankRepository.findByEnNameContaining(keyword, pageable)
    }

    // 전체 의사의 학력·자격면허 점수를 의대 순위 기준으로 일괄 계산하여 갱신
    fun updateEducationLicenseScores(): UpdateResult {
        val allDoctors = doctorRepository.findAll() // 전체 의사 목록 조회
        val universityList = universityRankRepository.findAll() // 전체 의대 순위 목록 조회
        
        // 점수 일괄 계산 및 결과 반환
        return educationLicenseScore.calculateForAllDoctors(allDoctors, universityList) 
    }

    // 전체 의사의 학력·자격면허 점수 총합을 계산하여 갱신
    fun updateTotalEducationLicenseScores(): UpdateResult {
        val allDoctors = doctorRepository.findAll() // 전체 의사 목록 조회

        return try {
            // 각 의사의 학력·자격면허 점수를 총합 및 계산
            allDoctors.forEach { doctor ->
                // 개별 점수 합산
                val total = doctor.educationLicenses.sumOf { it.educationLicense.score ?: 0.0 }

                doctor.totalEducationLicenseScore = total // 총합 점수 반영
                doctorRepository.save(doctor) // 변경된 의사 정보 저장
            }
            // 갱신 성공 응답 반환 
            UpdateResult(success = true, message = "의사 전체 학력 점수 총합 갱신 완료")
        } catch (e: Exception) {
            // 갱신 실패 응답 반환
            UpdateResult(success = false, message = "학력 점수 총합 갱신 실패: ${e.message}")
        }
    }

}
