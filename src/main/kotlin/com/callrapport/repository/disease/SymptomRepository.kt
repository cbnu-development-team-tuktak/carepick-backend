package com.callrapport.repository.disease

// 엔티티 관련 import
import com.callrapport.model.disease.Symptom // Symptom: 개별 증상 정보를 저장하는 엔티티

// Spring Data JPA 관련 import
import org.springframework.data.jpa.repository.JpaRepository // JPA의 CRUD 기능 제공
import org.springframework.stereotype.Repository // 레포지토리임을 나타내는 어노테이션

import org.springframework.data.domain.Page // 페이지네이션을 지원하는 JPA의 기본 객체 (검색 결과를 페이지 단위로 관리)
import org.springframework.data.domain.Pageable // 페이지네이션 요청을 처리하는 JPA 객체 (클라이언트가 요청한 페이지 정보 포함)

import org.springframework.data.jpa.repository.Query // 사용자 정의 JPQL 또는 네이티브 쿼리를 작성하기 위한 어노테이션
import org.springframework.data.repository.query.Param // @Query에서 파라미터를 바인딩하기 위한 어노테이션

@Repository
interface SymptomRepository : JpaRepository<Symptom, Long> {
    // 모든 증상 정보를 페이지네이션으로 조회
    override fun 
    findAll(
        pageable: Pageable // 페이지 요청 정보
    ): Page<Symptom> // 페이징된 증상 목록 반환

    // 증상명으로 증상 정보 조회
    fun findByName(
        name: String // 증상명 (예: 기침, 복통 등)
    ): Symptom? // 해당 이름을 가진 증상 엔티티 (없으면 null 반환)

    // 동일한 이름의 증상이 존재하는지 확인
    fun existsByName(
        name: String // 증상명
    ): Boolean // 중복 여부 (true: 존재함, false: 없음)

    // 특정 초성으로 시작하는 증상을 페이지 단위로 조회
    fun findByNameBetween(
        start: String, // 초성 범위 시작 (예: 가)
        end: String, // 초성 범위 끝 (예: 나)
        pageable: Pageable // 페이지 요청 정보
    ): Page<Symptom> // 조건에 해당하는 증상 목록 반환

    // 특정 초성 범위에 해당하는 증상의 개수 조회 (사용자 정의 쿼리)
    @Query("SELECT COUNT(s) FROM Symptom s WHERE s.name >= :start AND s.name < :end")
    fun countByNameRange(
        @Param("start") start: String, // 초성 범위 시작
        @Param("end") end: String // 초성 범위 끝
    ): Long // 조건에 부합하는 증상 개수 반환

    // 이름 목록에 포함된 증상 조회
    fun findByNameIn(
        names: List<String> // 증상명 리스트
    ): List<Symptom> // 해당 이름을 가진 증상 엔티티 목록
}
