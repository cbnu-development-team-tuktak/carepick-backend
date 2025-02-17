package com.callrapport.model.doctor

import jakarta.persistence.*

@Entity
@Table(name = "doctor_images")
data class DoctorImage(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val imageUrl: String, // 의사 프로필 사진 URL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    val doctor: Doctor // 의사와의 연관 관계 (N:1)
)