package com.callrapport.repository.doctor

// Model (엔티티) 관련 import 
import com.callrapport.model.doctor.Doctor // Doctor: 의사 정보를 저장하는 엔티티 (ID, 이름, 프로필 이미지, 진료과 등)
import com.callrapport.model.doctor.DoctorEducationLicense // DoctorEducationLicense: 의사의 학력 및 자격면허 정보를 저장하는 엔티티

// Spring Data JPA 관련 import
import org.springframework.stereotype.Repository // 해당 인터페이스가 데이터베이스 접근 레이어(Repository)임을 나타내는 어노테이션
import org.springframework.data.jpa.repository.JpaRepository // JPA에서 기본적인 CRUD (Create, Read, Update, Delete) 메서드를 제공하는 인터페이스

@Repository
interface DoctorEducationLicenseRepository : JpaRepository<DoctorEducationLicense, Long> {
    // 특정 의사(Doctor)에 해당하는 모든 자격면허 정보를 조회
    fun findByDoctor(
        doctor: Doctor // 검색할 대상인 Doctor 객체
    ): List<DoctorEducationLicense>? // 해당 Doctor 객체와 연관된 모든 DoctorEducationLicense 목록
}
