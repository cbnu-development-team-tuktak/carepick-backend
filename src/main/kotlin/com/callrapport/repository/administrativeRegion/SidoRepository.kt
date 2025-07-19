package com.callrapport.repository.administrativeRegion

import com.callrapport.model.administrativeRegion.Sido // JPA 엔티티 Sido를 가져오기
import org.springframework.data.jpa.repository.JpaRepository // Spring Data JPA의 JpaRepository를 상속받아 CRUD 기능을 제공
import org.springframework.stereotype.Repository 

// SidoRepository 인터페이스 정의
@Repository
interface SidoRepository : JpaRepository<Sido, String> {
    fun findByCode(code: String): Sido? // 코드로 Sido 엔티티를 조회하는 메소드
    fun findByName(name: String): Sido? // 이름으로 Sido 엔티티를 조회하는 메소드
}