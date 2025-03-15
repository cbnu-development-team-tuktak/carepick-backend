package com.callrapport.repository.hospital

// 엔티티 관련 import
import com.callrapport.model.hospital.Hospital // Hospital: 병원 정보를 저장하는 엔티티
import com.callrapport.model.hospital.HospitalSpecialty // HospitalSpecialty: 병원과 진료과의 관계를 저장하는 엔티티
import com.callrapport.model.common.Specialty // Specialty: 진료과 정보를 저장하는 엔티티

// Spring Data JPA 관련 import
import org.springframework.data.jpa.repository.JpaRepository // JPA에서 기본적인 CRUD (Create, Read, Update, Delete) 메서드를 제공하는 인터페이스
import org.springframework.stereotype.Repository // 해당 인터페이스가 데이터 접근 레이어(Repository)임을 나타내는 어노테이션

@Repository
interface HospitalSpecialtyRepository : JpaRepository<HospitalSpecialty, Long> {
    // 특정 병원의 모든 진료과 가져오기
    fun findByHospital(
        hospital: Hospital // 검색할 병원 객체
    ): List<HospitalSpecialty> // 해당 병원과 연관된 모든 HospitalSpecialty 목록
    
    // 특정 진료과를 갖고 있는 병원 검색
    fun findBySpecialty(
        specialty: Specialty // 검색할 진료과 객체
    ): List<HospitalSpecialty> // 해당 진료과와 연관된 모든 HospitalSpecialty 목록
}
