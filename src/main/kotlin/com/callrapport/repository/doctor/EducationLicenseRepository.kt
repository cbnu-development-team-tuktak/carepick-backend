package com.callrapport.repository.doctor

// 엔티티 관련 import 
import com.callrapport.model.doctor.EducationLicense // EducationLicense: 의사의 학력 및 자격면허 정보를 저장하는 엔티티 

// Spring Data JPA 관련 import 
import org.springframework.data.jpa.repository.JpaRepository // JPA에서 기본적인 CRUD (Create, Read, Update, Delete) 메서드를 제공하는 인터페이스
import org.springframework.stereotype.Repository // 해당 인터페이스가 데이터 접근 레이어(Repository)임을 나타내는 어노테이션
import org.springframework.data.domain.Page // 페이징 결과를 담는 객체
import org.springframework.data.domain.Pageable // 페이징 요청 정보를 담는 객체

@Repository
interface EducationLicenseRepository : JpaRepository<EducationLicense, Long> {
    // 자격면허 이름으로 조회
    fun findByName(
        name: String // 검색할 자격면허의 이름
    ): EducationLicense? // 해당 이름과 일치하는 EducationLicense 객체

    // 모든 자격면허를 페이지네이션으로 조회
    // 페이지네이션을 적용하기 위해 오버라이드
    override fun findAll(
        pageable: Pageable // 페이지네이션 정보를 포함한 객체
    ): Page<EducationLicense> // 페이지 단위의 모든 학력/자격면허 목록
}
