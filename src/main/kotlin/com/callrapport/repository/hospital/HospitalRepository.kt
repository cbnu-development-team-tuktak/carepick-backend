package com.callrapport.repository.hospital

// 엔티티 관련 import
import com.callrapport.model.hospital.Hospital // Hospital: 병원 정보를 저장하는 엔티티

// Spring Data JPA 관련 import
import org.springframework.data.jpa.repository.JpaRepository // JPA에서 기본적인 CRUD (Create, Read, Update, Delete) 메서드를 제공하는 인터페이스
import org.springframework.stereotype.Repository // 해당 인터페이스가 데이터 접근 레이어(Repository)임을 나타내는 어노테이션

// JPQL을 활용한 사용자 정의 쿼리 메서드 관련
import org.springframework.data.jpa.repository.Query // JPA에서 사용자 정의 JPQL (쿼리 메서드)를 작성할 때 사용하는 어노테이션
import org.springframework.data.repository.query.Param // @Query에서 JPQL의 매개변수를 바인딩할 때 사용하는 어노테이션

// 페이지네이션 관련 import
import org.springframework.data.domain.Page // 페이지네이션을 지원하는 JPA의 기본 객체
import org.springframework.data.domain.Pageable // 페이지네이션 요청을 처리하는 JPA 객체

@Repository
interface HospitalRepository : JpaRepository<Hospital, String> {
    // 병원 이름을 기준으로 검색
    // Like %keyword%: 부분 일치 검색을 수행하여 특정 키워드가 포함된 이름 검색 가능
    // Pageable을 사용하여 검색 결과를 페이지 단위로 반환
    @Query("SELECT h FROM Hospital h WHERE h.name LIKE %:keyword%")
    fun searchByName(
        @Param("keyword") keyword: String, // 검색할 병원 이름 키워드
        pageable: Pageable // 페이지네이션 정보를 포함한 객체
    ): Page<Hospital> // 페이지 단위의 검색된 병원 목록

    // 특정 주소를 기준으로 검색
    // Like %keyword%: 부분 일치 검색을 수행하여 특정 키워드가 포함된 이름 검색 가능
    // Pageable을 사용하여 검색 결과를 페이지 단위로 반환
    @Query("SELECT h FROM Hospital h WHERE h.address LIKE %:keyword%")
    fun searchByAddress(
        @Param("keyword") keyword: String, // 검색할 병원 주소 키워드
        pageable: Pageable // 페이지네이션 정보를 포함한 객체
    ): Page<Hospital> // 페이지 단위의 검색된 병원 목록

    // 모든 병원 정보를 페이지네이션으로 조회
    // 페이지네이션을 적용하기 위해 오버라이드
    override fun findAll(
        pageable: Pageable // 페이지네이션 정보를 포함한 객체
    ): Page<Hospital> // 페이지 단위의 전체 병원 목록
}