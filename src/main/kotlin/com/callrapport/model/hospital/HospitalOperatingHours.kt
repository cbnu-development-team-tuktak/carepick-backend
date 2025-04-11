package com.callrapport.model.hospital

// JPA 관련 import
import jakarta.persistence.*

@Entity
@Table(name = "hospital_operating_hours")
data class HospitalOperatingHours(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0, // 연결 테이블 기본 키

    // 병원과의 관계 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "hospital_id", 
        nullable = false
    )
    val hospital: Hospital,

    // 운영 시간과의 관계 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "operating_hours_id", 
        nullable = false
    )
    val operatingHours: OperatingHours
)
