package com.callrapport.model.hospital

// JPA 관련 import 
import jakarta.persistence.*

@Entity
@Table(name = "hospitals")
data class Hospital(
    @Id // 기본 키
    val id: String, // 병원 ID (기본키)

    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val name: String, // 병원 이름 

    @Column(nullable = true) // 선택적 입력 값 (NULL 허용)
    val phoneNumber: String?, // 전화번호

    @Column(nullable = true) // 선택적 입력 값 (NULL 허용)
    val homepage: String?, // 홈페이지 URL 

    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val address: String, // 병원 주소

    @Column(nullable = true) // 선택적 입력 값 (NULL 허용)
    val operatingHours: String?, 
    
    @OneToMany( // M:N 관계
        mappedBy = "hospital", 
        cascade = [CascadeType.ALL], 
        orphanRemoval = true
    )
    val specialties: List<HospitalSpecialty> = mutableListOf(),

    @OneToMany(
        mappedBy = "hospital",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    val doctors: List<HospitalDoctor> = mutableListOf(),

    @OneToOne(
        mappedBy = "hospital",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    val additionalInfo: HospitalAdditionalInfo? = null
)