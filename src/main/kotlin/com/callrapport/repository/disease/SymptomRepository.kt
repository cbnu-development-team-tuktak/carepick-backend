package com.callrapport.repository.disease

// 엔티티 관련 import
import com.callrapport.model.disease.Symptom // Symptom: 개별 증상 정보를 저장하는 엔티티

// Spring Data JPA 관련 import
import org.springframework.data.jpa.repository.JpaRepository // JPA의 CRUD 기능 제공
import org.springframework.stereotype.Repository // 레포지토리임을 나타내는 어노테이션

@Repository
interface SymptomRepository : JpaRepository<Symptom, Long> {
    // 증상명으로 증상 정보 조회
    fun findByName(
        name: String // 증상명 (예: 기침, 복통 등)
    ): Symptom? // 해당 이름을 가진 증상 엔티티 (없으면 null 반환)

    // 동일한 이름의 증상이 존재하는지 확인
    fun existsByName(
        name: String // 증상명
    ): Boolean // 중복 여부 (true: 존재함, false: 없음)
}
