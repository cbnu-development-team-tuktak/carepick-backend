package com.callrapport.repository.disease

// 엔티티 관련 import
import com.callrapport.model.disease.Category // Category: 질병의 분류 정보를 저장하는 엔티티

// Spring Data JPA 관련 import
import org.springframework.data.jpa.repository.JpaRepository // JPA 기반 데이터 액세스를 위한 인터페이스


interface CategoryRepository : JpaRepository<Category, Long> {
    // 분류명을 기준으로 조회 
    fun findByName(
        name: String // 분류명
    ): Category? // 이름에 해당하는 분류명
}
