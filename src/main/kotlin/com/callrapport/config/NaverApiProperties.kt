package com.callrapport.config
// Spring Boot의 외부 설정 값을 객체에 바인딩해주는 어노테이션
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "naver")
data class NaverApiProperties(
    var clientId: String = "zb92fols6g", // Naver API Client ID
    var secret: String = "D22P4B37g3yJpLYDqjrLMfqlqadJby3fLkipdncy" // Naver API Secert Key
)