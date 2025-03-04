package com.callrapport.repository.hospital

import com.callrapport.model.hospital.Hospital

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Repository
interface HospitalRepository : JpaRepository<Hospital, String> {
    // 병원 이름을 기준으로 검색하는 메서드
    // Like %keyword%: 부분 일치 검색을 수행하여 특정 키워드가 포함된 이름 검색 가능
    // Pageable을 사용하여 검색 결과를 페이지 단위로 반환
    @Query("SELECT h FROM Hospital h WHERE h.name LIKE %:keyword%")
    fun searchByName(
        @Param("keyword") keyword: String,
        pageable: Pageable
    ): Page<Hospital>

    // 특정 주소를 기준으로 검색하는 메서드
    // Like %keyword%: 부분 일치 검색을 수행하여 특정 키워드가 포함된 이름 검색 가능
    // Pageable을 사용하여 검색 결과를 페이지 단위로 반환
    @Query("SELECT h FROM Hospital h WHERE h.address LIKE %:keyword%")
    fun searchByAddress(
        @Param("keyword") keyword: String,
        pageable: Pageable
    ): Page<Hospital>

    // 모든 병원 정보를 페이지네이션으로 조회하는 메서드
    // 페이지네이션을 적용하기 위해 오버라이드
    override fun findAll(
        pageable: Pageable // 페이지네이션 정보를 포함한 객체
    ): Page<Hospital>
}