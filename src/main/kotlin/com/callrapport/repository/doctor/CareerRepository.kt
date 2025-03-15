package com.callrapport.repository.doctor

// 엔티티 관련 import
import com.callrapport.model.doctor.Career // Career: 의사의 경력 정보를 저장하는 엔티티

// Spring Data JPA 관련 import
import org.springframework.data.jpa.repository.JpaRepository // JPA 기반 데이터 액세스를 위한 인터페이스
import org.springframework.stereotype.Repository // 해당 인터페이스가 Repository 계층임을 명시하는 어노테이션

@Repository
interface CareerRepository : JpaRepository<Career, Long> {
    // 경력 내용을 이름(name)으로 조회
    fun findByName(
        name: String // 검색할 경력의 이름
    ): Career? // 해당 이름과 일치하는 Career 객체
}
