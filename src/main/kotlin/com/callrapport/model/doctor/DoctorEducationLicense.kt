package com.callrapport.model.doctor

// JPA 관련 import
import jakarta.persistence.* 

// JSON 직렬화 관련 import 
import com.fasterxml.jackson.annotation.JsonIgnore // 특정 필드를 JSON 변환 시 제외하는 어노테이션 (순환 참조 방지)

@Entity
@Table(name = "doctor_education_licenses") 
data class DoctorEducationLicense(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "education_licenses_id", nullable = false, referencedColumnName = "id")
    @JsonIgnore
    val educationLicense: EducationLicense,

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false, referencedColumnName = "id")
    @JsonIgnore
    val doctor: Doctor // ✅ `Doctor`와 연관 관계 설정
)