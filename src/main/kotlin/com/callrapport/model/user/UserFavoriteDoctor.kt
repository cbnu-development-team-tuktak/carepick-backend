package com.callrapport.model.user

import jakarta.persistence.*
import com.callrapport.model.doctor.Doctor

@Entity
@Table(name = "user_favorite_doctor")
data class UserFavoriteDoctor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null, 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User, // 연결된 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    val doctor: Doctor // 연결된 의사
)