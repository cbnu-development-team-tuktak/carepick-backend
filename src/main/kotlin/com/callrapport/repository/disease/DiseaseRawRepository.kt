package com.callrapport.repository.disease

// 엔티티 관련 import
import com.callrapport.model.disease.DiseaseRaw // DiseaseRaw: 질병 원본 데이터를 저장하는 엔티티
import com.callrapport.model.disease.DiseaseStatus // DiseaseStatus: 질병 처리 상태 enum 클래스

// Spring Data JPA 관련 import
import org.springframework.stereotype.Repository // 해당 인터페이스가 데이터베이스 접근 레이어(Repository)임을 나타내는 어노테이션
import org.springframework.data.jpa.repository.JpaRepository // JPA에서 기본적인 CRUD (Create, Read, Update, Delete) 메서드를 제공하는 인터페이스

@Repository 
interface DiseaseRawRepository : JpaRepository<DiseaseRaw, Long> {
    // 처리 상태(DiseaseStatus)를 기준으로 질병 리스트 조회
    fun findByStatus(
        status: DiseaseStatus // 처리 상태
    ): List<DiseaseRaw> // 특정 처리 상태에 해당하는 질병 원본 정보 목록
}