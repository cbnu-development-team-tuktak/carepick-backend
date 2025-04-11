package com.callrapport.repository.common

// Model (엔티티) 관련 import
import com.callrapport.model.common.AdministrativeRegion // 행정구역 엔티티

// Spring Data JPA 관련 import
import org.springframework.data.domain.Page // 페이징된 결과(Page<T>)를 표현하는 객체
import org.springframework.data.domain.Pageable // 페이징 요청 정보 (page, size, sort 등)
import org.springframework.data.jpa.repository.JpaRepository // JPA를 사용한 CRUD 리포지토리 인터페이스
import org.springframework.stereotype.Repository // 리포지토리 빈으로 등록하기 위한 어노테이션

import org.springframework.data.jpa.repository.Query

@Repository
interface AdministrativeRegionRepository : JpaRepository<AdministrativeRegion, Long> {
    // 시도명으로 검색
    fun findBySidoNm(
        sidoNm: String, // 시도명
        pageable: Pageable // 페이지 요청 정보
    ): Page<AdministrativeRegion> // 시도명과 일치하는 행정구역 정보 페이지

    // 시군구명으로 검색
    fun findBySggNm(
        sggNm: String, // 시군구명
        pageable: Pageable // 페이지 요청 정보
    ): Page<AdministrativeRegion> // 시군구명과 일치하는 행정구역 정보 페이지

    // 읍면동명으로 검색 
    fun findByUmdNm(
        umdNm: String, // 읍면동명
        pageable: Pageable // 페이지 요청 정보
    ): Page<AdministrativeRegion> // 읍면동명과 일치하는 행정구역 정보 페이지

    // 리명으로 검색 
    fun findByRiNm(
        riNm: String, // 리명
        pageable: Pageable // 페이지 요청 정보
    ): Page<AdministrativeRegion> // 리명과 일치하는 행정구역 정보 페이지
    
    // 시도명을 중복 없이 반환하는 쿼리 추가
    @Query("SELECT DISTINCT ar.sidoNm FROM AdministrativeRegion ar")
    fun findDistinctSidoNames(): List<String>
}
