package com.callrapport.model.hospital

import jakarta.persistence.*
import com.fasterxml.jackson.annotation.JsonManagedReference

@Entity
@Table(name = "hospitals")
data class Hospital(
    @Id
    val id: String, // 병원 ID (기본키)

    @Column(nullable = false)
    val name: String, // 병원 이름 

    @Column(nullable = true)
    val phoneNumber: String? = null, // 전화번호

    @Column(nullable = true)
    val homepage: String? = null, // 홈페이지 URL 

    @Column(nullable = true)
    val address: String? = null, // 병원 주소

    @Column(nullable = true)
    val operatingHours: String? = null,

    @Column(nullable = true)
    val url: String? = null, // 병원 정보 페이지 URL

    @OneToMany(mappedBy = "hospital", cascade = [CascadeType.ALL], orphanRemoval = true)
    val specialties: List<HospitalSpecialty> = mutableListOf(),

    @OneToMany(mappedBy = "hospital", cascade = [CascadeType.ALL], orphanRemoval = true)
    val doctors: List<HospitalDoctor> = mutableListOf(),

    @OneToOne(mappedBy = "hospital", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonManagedReference
    val additionalInfo: HospitalAdditionalInfo? = null
)
