package com.callrapport.repository.administrativeRegion

import org.springframework.data.jpa.repository.JpaRepository // Spring Data JPA의 JpaRepository를 상속받아 CRUD 기능을 제공
import org.springframework.stereotype.Repository 

import com.callrapport.model.administrativeRegion.SidoSgg // JPA 엔티티 SggUmd를 가져오기
import com.callrapport.model.administrativeRegion.Sido // Sido 엔티티를 가져오기
import com.callrapport.model.administrativeRegion.Sgg // Sgg 엔티티를 가져오기

// 페이징 관련 import
import org.springframework.data.domain.Pageable // 페이징 요청 정보를 담는 객체
import org.springframework.data.domain.Page // 페이징 결과를 담는 객체

// SidoSggRepository 인터페이스 정의
@Repository
interface SidoSggRepository : JpaRepository<SidoSgg, Long> {
    fun findBySido(sido: Sido, pageable: Pageable): Page<SidoSgg> // 특정 Sido에 속하는 SidoSgg 목록을 페이징 처리하여 조회
    fun findBySido(sido: Sido): List<SidoSgg> // 특정 Sido에 속하는 SidoSgg 목록을 조회

    fun findBySgg(sgg: Sgg, pageable: Pageable): Page<SidoSgg> // 특정 Sgg에 속하는 SidoSgg 목록을 페이징 처리하여 조회
    fun findBySgg(sgg: Sgg): List<SidoSgg> // 특정 Sgg에 속하는 SidoSgg 목록을 조회
}