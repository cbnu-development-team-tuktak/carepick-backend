package com.callrapport.model.doctor

// JPA 관련 import
import jakarta.persistence.* // JPA 엔티티 매핑을 위한 어노테이션 포함

// JSON 직렬화 관련 import 
import com.fasterxml.jackson.annotation.JsonIgnore // 특정 필드를 JSON 변환 시 제외하는 어노테이션 (순환 참조 방지)

@Entity
@Table(name = "doctor_education_licenses") 
data class DoctorEducationLicense(
    @Id // 기본 키(Primary Key) 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID 값을 자동 증가(Auto Increment)하도록 설정
    val id: Long? = null, // 의사 학력/자격면허 관계 ID (자동 생성됨)

    // 학력/자격면허(EducationLicense)와 다대일(N:1) 관계
    @ManyToOne // 여러 개의 DoctorEducationLicense가 하나의 EducationLicense에 연결될 수 있음
    @JoinColumn(
        name = "education_licenses_id", // 매핑할 외래 키(FK) 이름
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        referencedColumnName = "id" // EducationLicense 엔티티의 "id" 컬럼을 참조
    )
    @JsonIgnore // 순환 참조 방지
    val educationLicense: EducationLicense, // 의사의 학력/자격면허 정보

    // 의사(Doctor)와 다대일(N:1) 관계
    @ManyToOne // 여러 개의 DoctorEducationLicense가 하나의 Doctor에 연결될 수 있음
    @JoinColumn(
        name = "doctor_id", // 매핑할 외래 키(FK) 이름
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        referencedColumnName = "id" // Doctor 엔티티의 "id" 컬럼을 참조
    )
    @JsonIgnore // 순환 참조 방지
    val doctor: Doctor // 학력/자격면허를 보유한 의사
)