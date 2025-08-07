package com.callrapport.dto.request

data class UserUpdateRequest(
    val userId: String, // 사용자 ID
    val nickname: String? = null, // 닉네임
    val password: String? = null // 비밀번호 (암호화된 형태로 저장)
)