package com.callrapport.model.doctor

import com.fasterxml.jackson.annotation.JsonManagedReference

// Model (엔티티) 관련 import
import com.callrapport.model.common.Specialty // Specialty 엔티티: 의사의 진료과 정보를 저장하는 엔티티

// JPA 관련 import 
import jakarta.persistence.*

// JSON 직렬화 관련 import
import com.fasterxml.jackson.annotation.JsonIgnore // 특정 필드를 JSON 변환 시 제외하는 어노테이션 (순환 참조 방지)

@Entity
@Table(name = "doctors")
data class Doctor(
    @Id // 기본 키
    val id: String, // 의사 ID (기본키)

    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val name: String, // 의사 이름

    @Column(nullable = true) // 선택적 입력 값 (NULL 허용)
    val profileImage: String? = null, // 프로필 이미지 URL  

    @OneToMany(
        mappedBy = "doctor",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    val educationLicenses: MutableList<DoctorEducationLicense> = mutableListOf(), // 의사의 학력 및 자격면허 정보 

    // ✅ 의사와 진료과(N:M)를 연결하는 `DoctorSpecialty` 테이블과의 관계 (1:N)
    @OneToMany(
        mappedBy = "doctor",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @JsonManagedReference // 순환 참조 방지
    var specialties: MutableList<DoctorSpecialty> = mutableListOf() // 의사의 진료과 목록
)
