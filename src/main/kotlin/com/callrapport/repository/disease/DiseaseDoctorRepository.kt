package com.callrapport.repository.disease

// 엔티티 관련 import
import com.callrapport.model.disease.Disease // Disease: 정제된 질병 정보를 저장하는 엔티티
import com.callrapport.model.doctor.Doctor // Doctor: 의사 정보를 저장하는 엔티티
import com.callrapport.model.disease.DiseaseDoctor // DiseaseDoctor: 질병-의사 연결 정보를 저장하는 엔티티

// Spring Data JPA 관련 import
import org.springframework.data.jpa.repository.JpaRepository // JPA의 CRUD 기능 제공
import org.springframework.stereotype.Repository // 레포지토리임을 나타내는 어노테이션

@Repository
interface DiseaseDoctorRepository : JpaRepository<DiseaseDoctor, Long> {
    // 특정 질병에 연결된 모든 의사 조회
    fun findByDisease(
        disease: Disease // 질병 엔티티
    ): List<DiseaseDoctor> // 해당 질병과 연결된 의사 목록

    // 특정 의사에 연결된 모든 질병 조회
    fun findByDoctor(
        doctor: Doctor // 의사 엔티티
    ): List<DiseaseDoctor> // 해당 의사와 연결된 질병 목록

    // 특정 질병-의사 조합이 이미 존재하는지 확인
    fun existsByDiseaseAndDoctor(
        disease: Disease, // 질병 엔티티
        doctor: Doctor // 의사 엔티티
    ): Boolean // 이미 연결되어 있으면 true 반환
}
