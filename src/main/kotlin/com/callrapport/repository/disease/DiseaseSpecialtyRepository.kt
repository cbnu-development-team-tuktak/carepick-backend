package com.callrapport.repository.disease

// 엔티티 관련 import
import com.callrapport.model.disease.DiseaseSpecialty // DiseaseSpecialty: 질병-진료과 관계 정보를 저장하는 엔티티

// Spring Data JPA 관련 import
import org.springframework.data.jpa.repository.JpaRepository // JPA 기반 데이터 액세스를 위한 인터페이스


interface DiseaseSpecialtyRepository : JpaRepository<DiseaseSpecialty, Long>