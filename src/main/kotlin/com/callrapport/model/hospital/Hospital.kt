package com.callrapport.model.hospital

import jakarta.persistence.*

@Entity
@Table(name = "hospitals")
data class Hospital(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String, // 병원명

    @Column(nullable = false)
    val address: String, // 병원 주소

    @Column(nullable = true)
    val phone: String?, // 전화번호

    @Column(nullable = true)
    val website: String?, // 웹사이트 링크

    @Column(nullable = true)
    val department: String?, // 진료 과목

    @Column(nullable = true)
    val openingHours: String?, // 운영 시간

    @OneToMany(mappedBy = "hospital", cascade = [CascadeType.ALL], orphanRemoval = true)
    val images: List<HospitalImage> = mutableListOf() // 병원 사진 목록(1:N)
    
)