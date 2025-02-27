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

    @Column(nullable = false)
    val educationLicense: String, // ✅ 자격면허 정보

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    @JsonIgnore
    val doctor: Doctor // ✅ `Doctor`와 연관 관계 설정
)