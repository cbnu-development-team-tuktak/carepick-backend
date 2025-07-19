package com.callrapport.repository.administrativeRegion

import org.springframework.data.jpa.repository.JpaRepository // Spring Data JPA의 JpaRepository를 상속받아 CRUD 기능을 제공
import org.springframework.stereotype.Repository 
import com.callrapport.model.administrativeRegion.SggUmd // JPA 엔티티 SggUmd를 가져오기
import com.callrapport.model.administrativeRegion.Sgg // Sgg 엔티티를 가져오기
import com.callrapport.model.administrativeRegion.Umd // Umd 엔티티를 가져오기

// 페이징 관련 import
import org.springframework.data.domain.Page // 페이징 처리를 위한 Page 인터페이스
import org.springframework.data.domain.Pageable // 페이징 정보를 담는 Pageable 인터페이스

// SggUmdRepository 인터페이스 정의
@Repository
interface SggUmdRepository : JpaRepository<SggUmd, Long> {
    fun findByUmd(umd: Umd, pageable: Pageable): Page<SggUmd> // Umd 엔티티와 페이징 정보를 받아 SggUmd 엔티티를 조회하는 메소드
    fun findByUmd(umd: Umd): List<SggUmd> // Umd 엔티티를 받아 SggUmd 엔티티를 조회하는 메소드

    fun findBySgg(sgg: Sgg, pageable: Pageable): Page<SggUmd> // Sgg 엔티티와 페이징 정보를 받아 SggUmd 엔티티를 조회하는 메소드
    fun findBySgg(sgg: Sgg): List<SggUmd> // Sgg 엔티티를 받아 SggUmd 엔티티를 조회하는 메소드

    fun findAllBySgg_Name(sggName: String): List<SggUmd> // Sgg 이름으로 SggUmd 엔티티를 조회하는 메소드
}
