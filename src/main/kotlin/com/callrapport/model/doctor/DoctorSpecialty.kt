package com.callrapport.model.doctor

// JPA 관련 import
import jakarta.persistence.*

// 엔티티 관련 import
import com.callrapport.model.common.Specialty // Specialty: 진료과 정보를 저장하는 엔티티
import com.callrapport.model.doctor.Doctor // Doctor: 의사 정보를 저장하는 엔티티

// JSON 직렬화 관련 import
import com.fasterxml.jackson.annotation.JsonBackReference // JSON 변환 시 순환 참조를 위한 어노테이션

@Entity
@Table(name = "doctor_specialties")
data class DoctorSpecialty(
    @Id // 기본 키(Primary Key) 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID 값을 자동 증가(Auto Increment)하므로 설정
    val id: Long? = null, // 의사-진료과 관계 ID (자동 생성됨)

    // 의사(Doctor)와 다대일(N:1) 관계
    @ManyToOne // 여러 개의 DoctorSpecialty가 하나의 Doctor에 연결될 수 있음
    @JoinColumn(
        name = "doctor_id", // 매핑할 외래 키(FK) 이름
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        referencedColumnName = "id" // Doctor 엔티티의 "id" 컬럼을 참조
    )
    @JsonBackReference // 순환 참조 방지
    val doctor: Doctor, // 특정 진료과를 보유한 의사

    // 진료과(Specialty)와 다대일(N:1) 관계
    @ManyToOne // 여러 개의 DoctorSpecialty가 하나의 Specialty에 연결될 수 있음
    @JoinColumn(
        name = "specialty_id", // 매핑할 외래 키(FK) 이름
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        referencedColumnName = "id" // Specialty 엔티티의 "id" 컬럼을 참조
    )
    val specialty: Specialty // 의사의 진료과 정보
)
