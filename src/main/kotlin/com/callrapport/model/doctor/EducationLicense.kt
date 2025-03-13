package com.callrapport.model.doctor

import jakarta.persistence.*

@Entity
@Table(name = "education_licenses")
data class EducationLicense(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true) // ✅ 자격면허명은 중복 저장 방지
    val name: String
)
