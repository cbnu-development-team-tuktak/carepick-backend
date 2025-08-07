package com.callrapport.model.user

import jakarta.persistence.*
import java.time.LocalDateTime

import com.callrapport.model.user.*

@Entity
@Table(name = "user")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false, unique = true, length = 50)
    val userId: String, // 로그인용 ID

    @Column(name = "email", nullable = false, unique = true)
    val email: String, // 이메일 주소

    @Column(name = "password", nullable = false)
    val password: String, // 비밀번호 (암호화된 형태로 저장)
    
    @Column(name = "nickname", nullable = false, length = 30)
    val nickname: String, // 사용자 표시용 닉네임

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(), // 생성 시간

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true, // 활성 상태

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, optional = false)
    val userProfile: UserProfile? = null, // 사용자 프로필 정보

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, optional = false)
    val userOauth: UserOauth? = null, // OAuth 계정 정보 (선택적)

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val userFavoriteHospitals: MutableList<UserFavoriteHospital> = mutableListOf(), // 즐겨찾기한 병원 목록

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val userFavoriteDoctors: MutableList<UserFavoriteDoctor> = mutableListOf() // 즐겨찾기한 의사 목록
)
