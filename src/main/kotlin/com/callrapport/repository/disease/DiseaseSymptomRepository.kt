package com.callrapport.repository.disease

// 엔티티 관련 import
import com.callrapport.model.disease.Disease // Disease: 정제된 질병 정보를 저장하는 엔티티
import com.callrapport.model.disease.Symptom // Symptom: 증상 정보를 저장하는 엔티티
import com.callrapport.model.disease.DiseaseSymptom // DiseaseSymptom: 질병-증상 연결 정보를 저장하는 엔티티

// Spring Data JPA 관련 import
import org.springframework.data.jpa.repository.JpaRepository // JPA의 CRUD 기능 제공
import org.springframework.stereotype.Repository // 레포지토리임을 나타내는 어노테이션

@Repository
interface DiseaseSymptomRepository : JpaRepository<DiseaseSymptom, Long> {
    // 특정 질병에 연결된 모든 증상 조회
    fun findByDisease(
        disease: Disease // 질병 엔티티
    ): List<DiseaseSymptom> // 해당 질병과 연결된 증상 목록

    // 특정 증상에 연결된 모든 질병 조회
    fun findBySymptom(
        symptom: Symptom // 증상 엔티티
    ): List<DiseaseSymptom> // 해당 증상과 연결된 질병 목록

    // 특정 질병-증상 조합이 이미 존재하는지 확인
    fun existsByDiseaseAndSymptom(
        disease: Disease, // 질병 엔티티
        symptom: Symptom // 증상 엔티티
    ): Boolean // 이미 연결되어 있으면 true 반환

    // 🔥 여러 증상에 연결된 모든 질병-증상 관계 조회
    fun findBySymptomIn(symptoms: List<Symptom>): List<DiseaseSymptom>

    
    // ✅ 여러 질병에 연결된 모든 질병-증상 관계 조회 (신규 추가)
    fun findByDiseaseIn(diseases: List<Disease>): List<DiseaseSymptom>
}