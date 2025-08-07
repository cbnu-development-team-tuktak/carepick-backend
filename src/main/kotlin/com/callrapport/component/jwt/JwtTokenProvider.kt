package com.callrapport.component.jwt

import com.callrapport.dto.response.JwtToken // JWT 토큰 응답 DTO
import io.jsonwebtoken.Jwts // JWT 생성 및 검증 라이브러리
import io.jsonwebtoken.SignatureAlgorithm // JWT 서명 알고리즘
import io.jsonwebtoken.security.Keys // 키 생성 유틸리티
import org.springframework.stereotype.Component // 컴포넌트 어노테이션
import java.util.*

@Component
class JwtTokenProvider {
    private val secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256) // 비밀 키 생성
    private val accessTokenValidity = 60 * 60 * 1000L // 액세스 토큰 유효 기간 (1시간)
    private val refreshTokenValidity = 7 * 24 * 60 * 60 * 1000L // 리프레시 토큰 유효 기간 (7일)

    fun generateToken(userId: String): JwtToken {
        val now = Date()

        val accessToken = Jwts.builder()
            .setSubject(userId) // 사용자 ID를 서브젝트로 설정
            .setIssuedAt(now) // 발급 시간 설정
            .setExpiration(Date(now.time + accessTokenValidity)) // 만료 시간 설정
            .signWith(secretKey) // 서명
            .compact() // JWT 생성

        val refreshToken = Jwts.builder()
            .setSubject(userId) // 사용자 ID를 서브젝트로 설정
            .setIssuedAt(now) // 발급 시간 설정
            .setExpiration(Date(now.time + refreshTokenValidity)) // 만료 시간 설정
            .signWith(secretKey) // 서명
            .compact() // JWT 생성
        
        return JwtToken(accessToken, refreshToken) // JWT 토큰 응답 DTO 반환
    }
}