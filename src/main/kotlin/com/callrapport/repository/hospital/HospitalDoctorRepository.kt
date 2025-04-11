package com.callrapport.repository.hospital

// 엔티티 관련 import
import com.callrapport.model.hospital.Hospital // Hospital: 병원 정보를 저장하는 엔티티
import com.callrapport.model.hospital.HospitalDoctor // HospitalDoctor: 병원에 속한 의사 정보를 저장하는 엔티티

// Spring Data JPA 관련 import 
import org.springframework.data.jpa.repository.JpaRepository // JPA에서 기본적인 CRUD (Create, Read, Update, Delete) 메서드를 제공하는 인터페이스
import org.springframework.stereotype.Repository // 해당 인터페이스가 데이터 접근 레이어(Repository)임을 나타내는 어노테이션

@Repository
interface HospitalDoctorRepository : JpaRepository<HospitalDoctor, Long> {
    // 특정 병원에 소속된 의사 목록을 조회
    fun findByHospital(
        hospital: Hospital // 검색할 병원 객체
    ): List<HospitalDoctor> // 해당 병원과 연관된 모든 HospitalDoctor 목록

    // 병원 ID와 의사 ID 조합이 존재하는지 여부 확인
    fun existsByHospitalIdAndDoctorId(
        hospitalId: String, // 병원 ID
        doctorId: String // 의사 ID
    ): Boolean // 해당 조합이 존재하면 true, 아니면 false

    // 특정 의사 ID에 대해 첫 번째 병원-의사 관계 조회
    fun findFirstByDoctorId(doctorId: String): HospitalDoctor?
}