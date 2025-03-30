package com.callrapport.model.disease

// JPA 관련 import
import jakarta.persistence.* // JPA 매핑 어노테이션

@Entity
@Table(name = "symptoms")
data class Symptom(
    @Id // 기본 키(Primary Key) 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 ID
    val id: Long = 0L, // 증상 고유 ID

    @Column(nullable = false, unique = true, length = 50) // 필수, 중복 방지
    val name: String // 증상명 (예: 기침, 발열 등)
)
