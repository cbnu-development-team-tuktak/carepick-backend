package com.callrapport.model.user

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "profile")
data class Profile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null, // 프로필 ID
    
    @Column(name = "real_name")
    val realName: String, // 실제 이름

    @Column(name = "birth_date", nullable = false)
    val birthDate: LocalDate, // 생년월일

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    val gender: Gender, // 성별

    @OneToOne(mappedBy = "profile", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, optional = false)
    val profileAddress: ProfileAddress? = null // 프로필 주소
)

enum class Gender {
    MALE, FEMALE, OTHER
}