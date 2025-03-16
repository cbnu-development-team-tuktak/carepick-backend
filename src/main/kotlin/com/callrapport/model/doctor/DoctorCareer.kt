package com.callrapport.model.doctor

// JPA 관련 import
import jakarta.persistence.* // JPA 엔티티 매핑을 위한 어노테이션 포함

// JSON 직렬화 관련 import
import com.fasterxml.jackson.annotation.JsonIgnore // JSON 변환 시 특정 필드를 제외하여 순환 참조 방지

@Entity
@Table(name = "doctor_careers")
data class DoctorCareer(
    @Id // 기본 키(Primary Key) 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID 값을 자동 증가(Auto Increment)하도록 설정
    val id: Long? = null,  // 의사 경력 ID (자동 생성됨)

    // 의사(Doctor)와 다대일(N:1) 관계
    @ManyToOne // 여러 개의 DoctorCareer가 하나의 Doctor에 연결될 수 있음
    @JoinColumn(
        name = "doctor_id", // 매핑할 외래 키(FK) 이름
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        referencedColumnName = "id" // Doctor 엔티티의 "id" 컬럼을 참조
    )
    @JsonIgnore // 순환 참조 방지
    val doctor: Doctor, // 경력 정보를 보유한 의사

    // 경력(Career)과 다대일(N:1) 관계
    @ManyToOne // 여러 개의 DoctorCareer가 하나의 Career에 연결될 수 있음
    @JoinColumn(
        name = "career_id", // 매핑할 외래 키(FK) 이름
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        referencedColumnName = "id" // Career 엔티티의 "id" 컬럼을 참조
    )
    @JsonIgnore // 순환 참조 방지 (JSON 변환 시 해당 필드 제외)
    val career: Career // 의사의 경력 정보
)