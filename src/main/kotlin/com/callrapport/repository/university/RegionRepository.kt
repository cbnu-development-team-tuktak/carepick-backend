package com.callrapport.repository.university

// Model (엔티티) 관련 import
import com.callrapport.model.university.Region // 지역 정보를 나타내는 엔티티

// Spring 및 JPA 관련 import
import org.springframework.data.jpa.repository.JpaRepository // JPA 기반 CRUD 기능을 제공하는 인터페이스
import org.springframework.stereotype.Repository // 해당 인터페이스를 Spring Repository 컴포넌트로 등록하는 어노테이션

@Repository
interface RegionRepository : JpaRepository<Region, Long> {
    // 지역 이름으로 Region 엔티티를 조회
    fun findByName(
        name: String // 조회할 지역 이름
    ): Region?
}
