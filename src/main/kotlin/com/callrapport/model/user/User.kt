package com.callrapport.model.user

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id // 기본키
    val id: String, // 유저 ID

    @Column(nullable =  false) // 필수 입력 값 (NULL 허용 안 함)
    val password: String, // 비밀번호

    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val name: String, // 유저 이름

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    val gender: Gender? = null,

    @Column(nullable = true) 
    val birthDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val status: UserStatus = UserStatus.ACTIVE,

    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun updateTimestamp() {
        updatedAt = LocalDateTime.now()
    }
}

enum class Gender {
    MALE, FEMALE, OTHER
}

enum class UserStatus {
    ACTIVE, SUSPENDED, DEACTIVATED
}