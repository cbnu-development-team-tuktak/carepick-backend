package com.callrapport.model.hospital

// JPA 및 JSON 관련 import
import jakarta.persistence.*

import java.time.LocalTime

@Entity
@Table(name = "operating_hours")
data class OperatingHours(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 ID
    val id: Long = 0, // 운영 시간 ID (기본 키)

    @Column(nullable = false)
    val day: String, // 요일 (예: "월", "화", ..., "공휴일")

    @Column(nullable = true)
    var startTime: LocalTime? = null, // 시작 시간 (예: 09:00)

    @Column(nullable = true)
    var endTime: LocalTime? = null // 종료 시간 (예: 18:00)
)
