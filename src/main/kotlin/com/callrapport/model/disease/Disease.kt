package com.callrapport.model.disease

import jakarta.persistence.*

@Entity
@Table(name = "diseases")
class Disease(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val nameKr: String, // 질병명(한국어)
 
    @Column(nullable = true, unique = true)
    val nameEn: String?, // 질병명(영어)

    @Column(nullable = true, unique = true)
    val diseaseCode: String?, // 질병 코드

    @Column(nullable = true, length = 2000)
    val definition: String?, // 질병 정의
    
    @Column(nullable = true, length = 2000)
    val symptoms: String?, // 증상

    @Column(nullable = true, length = 2000)
    val causes: String?, // 원인

    @Column(nullable = true, length = 2000)
    val diagnosis: String?, // 진단 방법

    @Column(nullable = true, length = 2000)
    val treatment: String?, // 치료법

    @Column(nullable = true, length = 2000)
    val prevention: String?, // 예방 방법
)