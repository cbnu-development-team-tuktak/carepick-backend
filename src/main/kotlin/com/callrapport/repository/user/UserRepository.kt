package com.callrapport.repository.user

import com.callrapport.model.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUserId(userId: String): User? // 사용자 ID로 사용자 조회
    fun findByEmail(email: String): User? // 이메일로 사용자 조회
    fun existsByUserId(userId: String): Boolean // 사용자 ID로 존재 여부 확인
    fun existsByEmail(email: String): Boolean // 이메일로 존재 여부 확인
}