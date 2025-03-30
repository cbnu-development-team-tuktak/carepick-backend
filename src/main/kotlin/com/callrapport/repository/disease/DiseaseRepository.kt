package com.callrapport.repository.disease

// 엔티티 관련 import
import com.callrapport.model.disease.Disease // Disease: 정제된 질병 정보를 저장하는 엔티티

// Spring Data JPA 관련 import
import org.springframework.data.jpa.repository.JpaRepository // JPA의 CRUD 기능 제공
import org.springframework.stereotype.Repository // 레포지토리임을 나타내는 어노테이션

@Repository
interface DiseaseRepository : JpaRepository<Disease, Long> {
    // 질병명으로 질병 정보 검색
    fun findByName(
        name: String // 질병명
    ): Disease? // 입력한 질병명과 정확히 일치하는 질병 정보
}
