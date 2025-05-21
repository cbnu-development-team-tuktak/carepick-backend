package com.callrapport.repository.university

// Model (엔티티) 관련 import
import com.callrapport.model.university.UniversityRank // 대학 랭킹 정보를 나타내는 엔티티

// Spring 및 JPA 관련 import
import org.springframework.data.domain.Page // 페이지 단위 조회 결과를 표현하는 JPA 객체
import org.springframework.data.domain.Pageable // 페이지 요청 정보를 담는 JPA 객체
import org.springframework.data.jpa.repository.JpaRepository // JPA 기반 CRUD 기능을 제공하는 인터페이스
import org.springframework.stereotype.Repository // 해당 인터페이스를 Spring Repository 컴포넌트로 등록하는 어노테이션

@Repository
interface UniversityRankRepository : JpaRepository<UniversityRank, Int> {
    // 정확한 한글 대학명으로 대학 랭킹 정보를 조회
    fun findByKrName(
        krName: String // 조회할 정확한 한글 대학명
    ): UniversityRank?
    
    // 한글 대학명에 해당 키워드가 포함된 대학들을 페이지네이션으로 조회
    fun findByKrNameContaining(
        krName: String, // 검색할 한글 대학명 키워드 
        pageable: Pageable // 페이지네이션 정보를 포함한 객체
    ): Page<UniversityRank>
    
    // 영어 대학명에 해당 키워드가 포함된 대학들을 페이지네이션으로 조회
    fun findByEnNameContaining(
        enName: String, // 검색할 영어 대학명 키워드
        pageable: Pageable // 페이지네이션 정보를 포함한 객체
    ): Page<UniversityRank>
    
    // 해당 한글 대학명이 존재하는지 여부를 확인
    fun existsByKrName(
        krName: String // 확인할 한글 대학명
    ): Boolean
    
    // 해당 영어 대학명이 존재하는지 여부를 확인
    fun existsByEnName(
        enName: String // 확인할 영어 대학명
    ): Boolean
}