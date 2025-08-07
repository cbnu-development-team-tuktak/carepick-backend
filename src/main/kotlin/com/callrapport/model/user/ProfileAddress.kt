package com.callrapport.model.user

import jakarta.persistence.*
import com.callrapport.model.user.Profile
import com.callrapport.model.user.Address

@Entity
@Table(name = "profile_address")
data class ProfileAddress(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne
    @JoinColumn(name = "profile_id", nullable = false)
    val profile: Profile, // 연결된 프로필

    @OneToOne
    @JoinColumn(name = "address_id", nullable = false)
    val address: Address // 연결된 주소
)