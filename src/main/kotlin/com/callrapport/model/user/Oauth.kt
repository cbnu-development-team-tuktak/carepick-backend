package com.callrapport.model.user

import jakarta.persistence.*

@Entity
@Table(name = "oauth")
data class Oauth(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    val provider: OauthProvider, // OAuth 제공자

    @Column(name = "provider_user_id", nullable = false, unique = true)
    val providerUserId: String, // ex: 카카오/구글 사용자 고유 ID

    @Column(name = "email")
    val email: String? = null, // SSO에서 제공된 이메일

    @Column(name = "nickname")
    val nickname: String? = null, // SSO에서 제공된 닉네임

    @Column(name = "profile_image_url")
    val profileImageUrl: String? = null // 프로필 이미지 URL
)

enum class OauthProvider {
    GOOGLE, KAKAO, NAVER, APPLE
}
