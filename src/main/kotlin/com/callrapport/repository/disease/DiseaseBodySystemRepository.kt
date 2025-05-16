package com.callrapport.repository.disease

// 엔티티 관련 import
import com.callrapport.model.disease.DiseaseBodySystem // DiseaseBodySystem: 질병-신체계통 관계 엔티티

// Spring Data JPA 관련 import
import org.springframework.data.jpa.repository.JpaRepository // JPA 기반 데이터 액세스를 위한 인터페이스


interface DiseaseBodySystemRepository : JpaRepository<DiseaseBodySystem, Long>
