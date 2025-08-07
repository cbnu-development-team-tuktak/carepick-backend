package com.callrapport.model.user

import com.callrapport.model.user.User
import com.callrapport.model.user.Oauth
import jakarta.persistence.*

@Entity
@Table(name = "user_oauth")
data class UserOauth(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User, // 연결된 사용자

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oauth_id", nullable = false)
    val oauth: Oauth // Oauth 계정
)