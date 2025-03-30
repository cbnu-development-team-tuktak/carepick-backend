package com.callrapport.repository.disease

// 엔티티 관련 import
import com.callrapport.model.disease.Disease // Disease: 정제된 질병 정보를 저장하는 엔티티
import com.callrapport.model.common.Specialty // Specialty: 진료과 정보를 저장하는 엔티티
import com.callrapport.model.disease.DiseaseSpecialty // DiseaseSpecialty: 질병-진료과 연결 정보를 저장하는 엔티티

// Spring Data JPA 관련 import
import org.springframework.data.jpa.repository.JpaRepository // JPA의 CRUD 기능 제공
import org.springframework.stereotype.Repository // 레포지토리임을 나타내는 어노테이션

@Repository
interface DiseaseSpecialtyRepository : JpaRepository<DiseaseSpecialty, Long> {
    // 특정 질병에 연결된 모든 진료과 조회
    fun findByDisease(
        disease: Disease // 질병 엔티티
    ): List<DiseaseSpecialty> // 해당 질병과 연결된 진료과 목록

    // 특정 진료과에 연결된 모든 질병 조회
    fun findBySpecialty(
        specialty: Specialty // 진료과 엔티티
    ): List<DiseaseSpecialty> // 해당 진료과와 연결된 질병 목록

    // 특정 질병-진료과 조합이 이미 존재하는지 확인
    fun existsByDiseaseAndSpecialty(
        disease: Disease, // 질병 엔티티
        specialty: Specialty // 진료과 엔티티
    ): Boolean // 이미 연결되어 있으면 true 반환
}