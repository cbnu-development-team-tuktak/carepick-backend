package com.callrapport.model.doctor

import jakarta.persistence.*

@Entity
@Table(name = "famous_doctors")
class FamousDoctor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null, // 기본 키 (자동 생성)

    // --- 우리 DB 의사와 매칭하기 위한 핵심 정보 (3가지 조합) ---
    @Column(nullable = false)
    val name: String, // 크롤링한 의사 이름

    @Column(nullable = false)
    val hospitalName: String, // 크롤링한 의사의 소속 병원 이름

    @Column(nullable = false)
    val specialtyName: String, // 크롤링한 의사의 진료과 이름
)