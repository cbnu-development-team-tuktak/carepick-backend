package com.callrapport.model.disease

// JPA 관련 import 
import jakarta.persistence.*

@Entity
@Table(name = "body_systems")
data class BodySystem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 ID
    val id: Long = 0L, // 신체계통 ID

    @Column(
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        unique = true // 중복 값 허용 안 함
    )
    val name: String // 신체계통 이름 (예: 호흡기계, 소화기계 등)
)
