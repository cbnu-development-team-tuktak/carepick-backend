package com.callrapport.repository.university

// Model (엔티티) 관련 import
import com.callrapport.model.university.UniversityRankRegion // 대학과 지역 간의 매핑 정보를 나타내는 엔티티

// Spring 및 JPA 관련 import
import org.springframework.data.jpa.repository.JpaRepository // JPA 기반 CRUD 기능을 제공하는 인터페이스
import org.springframework.stereotype.Repository // 해당 인터페이스를 Spring Repository 컴포넌트로 등록하는 어노테이션

@Repository
interface UniversityRankRegionRepository : JpaRepository<UniversityRankRegion, Long>
