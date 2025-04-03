package com.callrapport.repository.disease

// 엔티티 관련 import
import com.callrapport.model.disease.Symptom // Symptom: 개별 증상 정보를 저장하는 엔티티

// Spring Data JPA 관련 import
import org.springframework.data.jpa.repository.JpaRepository // JPA의 CRUD 기능 제공
import org.springframework.stereotype.Repository // 레포지토리임을 나타내는 어노테이션

import org.springframework.data.domain.Page // 페이지네이션을 지원하는 JPA의 기본 객체 (검색 결과를 페이지 단위로 관리)
import org.springframework.data.domain.Pageable // 페이지네이션 요청을 처리하는 JPA 객체 (클라이언트가 요청한 페이지 정보 포함)

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

@Repository
interface SymptomRepository : JpaRepository<Symptom, Long> {

    // 모든 증상 정보를 페이지네이션으로 조회
    override fun findAll(pageable: Pageable): Page<Symptom>

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
        start: String,
        end: String,
        pageable: Pageable
    ): Page<Symptom>

    @Query("SELECT COUNT(s) FROM Symptom s WHERE s.name >= :start AND s.name < :end")
    fun countByNameRange(@Param("start") start: String, @Param("end") end: String): Long
}
