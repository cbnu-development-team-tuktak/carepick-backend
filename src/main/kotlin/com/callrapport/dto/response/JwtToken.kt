package com.callrapport.dto.response

data class JwtToken(
    val accessToken: String, // 액세스 토큰
    val refreshToken: String, // 리프레시 토큰
)