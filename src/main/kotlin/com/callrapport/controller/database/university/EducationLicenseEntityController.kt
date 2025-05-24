package com.callrapport.controller.database.university

// DTO 관련 import
import com.callrapport.dto.UniversityRankDetailsResponse // 대학 랭킹 상세 정보를 담는 응답 DTO

// Service 계층 관련 import
import com.callrapport.service.university.UniversityRankService // 대학 랭킹 비즈니스 로직을 처리하는 서비스

// Spring 및 JPA 관련 import
import org.springframework.data.domain.Page // 페이지 단위 조회 결과를 표현하는 JPA 객체
import org.springframework.data.domain.Pageable // 페이지 요청 정보를 담는 JPA 객체
import org.springframework.http.ResponseEntity // HTTP 응답 본문 및 상태 코드를 포함하는 클래스
import org.springframework.web.bind.annotation.* // REST 컨트롤러와 관련된 어노테이션 모음 (RequestMapping, GetMapping 등)

// 계산 로직 관련 import
import com.callrapport.component.calculator.EducationLicenseScore.UpdateResult // 학력 점수 업데이트 결과 DTO

// Repository 관련 import
import com.callrapport.repository.doctor.EducationLicenseRepository // 학력/자격면허 리포지토리

// 모델 관련 import
import com.callrapport.model.doctor.EducationLicense

@RestController
@RequestMapping("/api/education-license")
class EducationLicenseEntityController(
    private val universityRankService: UniversityRankService,
    private val educationLicenseRepository: EducationLicenseRepository
) {
    // 전체 학력/자격면허 목록 조회 (페이지네이션)
    // 예: http://localhost:8080/api/education-license?page=0&size=10
    @GetMapping
    fun getAllEducationLicenses(
        pageable: Pageable // 페이지네이션 정보를 포함한 객체
    ): Page<EducationLicense> {
        return educationLicenseRepository.findAll(pageable)
    }

    // 학력 점수 갱신
    // 예: http://localhost:8080/api/education-license/update-scores    
    @GetMapping("/education-license/update-scores")
    fun updateAllDoctorEducationScores(): ResponseEntity<UpdateResult> {
        val result = universityRankService.updateEducationLicenseScores()
        return if (result.success) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.status(500).body(result)
        }
    }
}
