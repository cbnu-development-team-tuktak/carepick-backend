package com.callrapport.model.common

// JPA 관련 import 
import jakarta.persistence.*

@Entity
@Table(name = "specialties") 
data class Specialty(
    @Id // 기본 키
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가
    var id: Long? = null, // 진료과 ID

    @Column(
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        unique = true // 동일한 진료과 이름 중복 저장 방지
    )
    val name: String // 진료과 이름 (내과, 정형외과 등)
)
