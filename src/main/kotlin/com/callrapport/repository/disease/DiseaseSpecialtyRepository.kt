package com.callrapport.repository.disease

// 엔티티 관련 import
import com.callrapport.model.disease.Disease // Disease: 질병 정보를 저장하는 엔티티
import com.callrapport.model.disease.DiseaseSpecialty // DiseaseSpecialty: 질병-진료과 관계 정보를 저장하는 엔티티

// Spring Data JPA 관련 import
import org.springframework.data.jpa.repository.JpaRepository // JPA 기반 데이터 액세스를 위한 인터페이스

interface DiseaseSpecialtyRepository : JpaRepository<DiseaseSpecialty, Long> {
    // 특정 질병에 연결된 진료과 관계를 조회
    fun findByDisease(
        disease: Disease // 조회할 질병 엔티티
    ): List<DiseaseSpecialty> // 해당 질병과 연결된 진료과 관계 목록
}