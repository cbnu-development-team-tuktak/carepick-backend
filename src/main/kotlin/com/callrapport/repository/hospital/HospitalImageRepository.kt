package com.callrapport.repository.hospital

// 엔티티 관련 import 
import com.callrapport.model.hospital.HospitalImage // HospitalImage: 병원의 이미지 정보를 저장하는 엔티티

// Spring Data JPA 관련 import 
import org.springframework.data.jpa.repository.JpaRepository // JPA에서 기본적인 CRUD (Create, Read, Update, Delete) 메서드를 제공하는 인터페이스
import org.springframework.stereotype.Repository // 해당 인터페이스가 데이터 접근 레이어(Repository)임을 나타내는 어노테이션

@Repository
interface HospitalImageRepository : JpaRepository<HospitalImage, String> {
    fun existsByHospitalIdAndImageId(
        hospitalId: String, // 병원 ID
        imageId: Long // 이미지 ID
    ): Boolean // 해당 조합이 존재하면 true, 아니면은 false
}
