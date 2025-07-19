package com.callrapport.repository.administrativeRegion

import com.callrapport.model.administrativeRegion.Sgg // JPA 엔티티 Sgg를 가져오기
import org.springframework.data.jpa.repository.JpaRepository // Spring Data JPA의 JpaRepository를 상속받아 CRUD 기능을 제공
import org.springframework.stereotype.Repository 

// SggRepository 인터페이스 정의
@Repository
interface SggRepository : JpaRepository<Sgg, String> {
    fun findByCode(code: String): Sgg? // 코드로 Sgg 엔티티를 찾는 메소드
    fun findByName(name: String): Sgg? // 이름으로 Sgg 엔티티를 찾는 메소드
    fun findAllByName(name : String): List<Sgg> // 이름으로 Sgg 엔티티 리스트를 찾는 메소드
}
