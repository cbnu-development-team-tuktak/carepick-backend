package com.callrapport.model.hospital

import jakarta.persistence.*

@Entity
@Table(name = "hospital_images")
class HospitalImage(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val imageUrl: String, // 병원 사진 URL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    val hospital: Hospital // 병원과 연관 관계
)
