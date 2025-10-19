package com.callrapport.repository.administrativeRegion

import com.callrapport.model.administrativeRegion.Sgg // JPA 엔티티 Sgg를 가져오기
import org.springframework.data.jpa.repository.JpaRepository // Spring Data JPA의 JpaRepository를 상속받아 CRUD 기능을 제공
import org.springframework.stereotype.Repository 

// SggRepository 인터페이스 정의
@Repository
interface SggRepository : JpaRepository<Sgg, Long> {
    fun findByCode(code: String): Sgg?
    fun findByName(name: String): Sgg?
    fun findAllByName(name: String): List<Sgg>

    fun findAllByNameContaining(name: String): List<Sgg> 
}