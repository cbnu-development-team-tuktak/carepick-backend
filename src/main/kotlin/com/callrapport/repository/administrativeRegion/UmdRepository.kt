package com.callrapport.repository.administrativeRegion

import com.callrapport.model.administrativeRegion.Umd // JPA 엔티티 Umd를 가져오기
import org.springframework.data.jpa.repository.JpaRepository // Spring Data JPA의 JpaRepository를 상속받아 CRUD 기능을 제공
import org.springframework.stereotype.Repository 

// UmdRepository 인터페이스 정의
@Repository
interface UmdRepository : JpaRepository<Umd, String> {
    fun findByCode(code: String): Umd? // 코드로 Umd 엔티티를 찾는 메소드
}