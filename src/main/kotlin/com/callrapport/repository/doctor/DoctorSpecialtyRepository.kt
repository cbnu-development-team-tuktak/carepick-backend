package com.callrapport.repository.doctor

// 엔티티 관련 import
import com.callrapport.model.doctor.DoctorSpecialty // DoctorSpecialty: 의사의 진료과 정보를 저장하는 엔티티

// Spring Data JPA 관련 import
import org.springframework.data.jpa.repository.JpaRepository // JPA에서 기본적인 CRUD (Create, Read, Update, Delete) 메서드를 제공하는 인터페이스
import org.springframework.stereotype.Repository // 해당 인터페이스가 데이터 접근 레이어(Repository)임을 나타내는 어노테이션

@Repository
interface DoctorSpecialtyRepository : JpaRepository<DoctorSpecialty, Long> {
    // 의사 ID를 기준으로 해당 의사와 연결된 진료과 목록을 조회
    fun findByDoctorId(
        doctorId: String // 검색할 대상인 Doctor의 ID
    ): List<DoctorSpecialty> // 해당 Doctor ID와 연관된 모든 DoctorSpecialty 목록
    
    // 진료과 ID를 기준으로 해당 진료과와 연결된 의사 목록을 조회
    fun findBySpecialtyId(
        specialtyId: String // 검색할 대상인 Specialty의 ID
    ): List<DoctorSpecialty> // 해당 Specialty ID와 연관된 모든 DoctorSpecialty 목록

    // 의사 ID와 진료과 ID 조합이 존재하는지 여부 확인
    fun existsByDoctorIdAndSpecialtyId(
        doctorId: String, // 의사 ID
        specialtyId: String // 진료과 ID
    ): Boolean // 해당 조합이 존재하면 true, 아니면 false
}
