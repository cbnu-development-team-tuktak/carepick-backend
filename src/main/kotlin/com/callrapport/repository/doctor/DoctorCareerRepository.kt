package com.callrapport.repository.doctor

// 엔티티 관련 import 
import com.callrapport.model.doctor.Doctor // Doctor: 의사 정보를 저장하는 엔티티
import com.callrapport.model.doctor.DoctorCareer // DotorCareer: 의사의 경력 정보를 저장하는 엔티티

// Spring Data JPA 관련 import
import org.springframework.data.jpa.repository.JpaRepository // JPA 기반 데이터 액세스를 위한 인터페이스

// Spring 컴포넌트 관련 import 
import org.springframework.stereotype.Repository // 해당 인터페이스가 Repository 계층임을 명시하는 어노테이션

@Repository
interface DoctorCareerRepository : JpaRepository<DoctorCareer, Long> {
    // 특정 의사(Doctor)에 해당하는 모든 경력 정보를 조회
    fun findByDoctor(
        doctor: Doctor // 검색할 Doctor 객체
    ): List<DoctorCareer>? // 해당 Doctor 객체와 연관된 모든 DoctorCareer 목록
}