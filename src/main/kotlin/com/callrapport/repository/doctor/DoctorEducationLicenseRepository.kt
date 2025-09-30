package com.callrapport.repository.doctor

// Model (엔티티) 관련 import 
import com.callrapport.model.doctor.Doctor // Doctor: 의사 정보를 저장하는 엔티티 (ID, 이름, 프로필 이미지, 진료과 등)
import com.callrapport.model.doctor.DoctorEducationLicense // DoctorEducationLicense: 의사의 학력 및 자격면허 정보를 저장하는 엔티티

// JPA 관련 import
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

// Spring Data JPA 관련 import
import org.springframework.stereotype.Repository // 해당 인터페이스가 데이터베이스 접근 레이어(Repository)임을 나타내는 어노테이션
import org.springframework.data.jpa.repository.JpaRepository // JPA에서 기본적인 CRUD (Create, Read, Update, Delete) 메서드를 제공하는 인터페이스

@Repository
interface DoctorEducationLicenseRepository : JpaRepository<DoctorEducationLicense, Long> {
    // 특정 의사(Doctor)에 해당하는 모든 자격면허 정보를 조회
    fun findByDoctor(
        doctor: Doctor // 검색할 대상인 Doctor 객체
    ): List<DoctorEducationLicense>? // 해당 Doctor 객체와 연관된 모든 DoctorEducationLicense 목록

    // 특정 의사 ID와 특정 자격면허 ID 조합이 존재하는지 여부 확인
    fun existsByDoctorIdAndEducationLicenseId(
        doctorId: String, // 의사 ID
        educationLicenseId: Long // 자격면허 ID
    ): Boolean // 해당 조합이 존재하면 true, 아니면 false

    @Modifying
    @Query(
        value = "INSERT IGNORE INTO doctor_education_licenses (doctor_id, education_licenses_id) VALUES (:doctorId, :licenseId)", // <-- 's' 추가
        nativeQuery = true
    )
    fun saveWithIgnore(@Param("doctorId") doctorId: String, @Param("licenseId") licenseId: Long)
}
