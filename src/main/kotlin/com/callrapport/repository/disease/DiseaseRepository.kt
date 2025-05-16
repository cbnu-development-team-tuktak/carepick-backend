package com.callrapport.repository.disease

// 엔티티 관련 import
import com.callrapport.model.disease.Disease // Disease: 질병 정보를 저장하는 엔티티

// Spring Data JPA 관련 import
import org.springframework.data.jpa.repository.JpaRepository // JPA 기반 데이터 액세스를 위한 인터페이스
import org.springframework.data.domain.Page // 페이지 응답 객체
import org.springframework.data.domain.Pageable // 페이지 요청 객체

interface DiseaseRepository : JpaRepository<Disease, Long> {
    // 질병명을 기준으로 단일 질병 조회
    fun findByName(
        name: String // 질병명
    ): Disease? // 질병명에 해당하는 질병

    // 여러 질병명을 기준으로 질병 목록 조회
    fun findByNameIn(
        name: List<String> // 질병명 리스트
    ): List<Disease> // 해당하는 질병 목록

    // 질병명을 기준으로 부분 검색
    fun findByNameContainingIgnoreCase(
        name: String, // 검색할 질병명 키워드
        pageable: Pageable // 페이지 요청 정보
    ): Page<Disease> // 검색된 질병 목록

    // 특정 신체계통에 해당하는 모든 질병 조회
    fun findByDiseaseBodySystem_BodySystem_Name(
        bodySystemName: String, // 신체계통 이름
        pageable: Pageable // 페이지 요청 정보
    ): Page<Disease> // 특정 신체계통에 해당하는 질병 목록

    // 특정 분류에 해당하는 모든 질병 조회
    fun findByDiseaseCategory_Category_Name(
        categoryName: String, // 분류 이름
        pageable: Pageable // 페이지 요청 정보
    ): Page<Disease> // 특정 분류에 해당하는 질병 목록

    // 특정 진료과에서 진료 가능한 모든 질병 조회
    fun findByDiseaseSpecialties_Specialty_Name(
        specialtyName: String, // 진료과 이름
        pageable: Pageable // 페이지 요청 정보
    ): Page<Disease> // 특정 진료과에서 진료 가능한 질병 목록
}
