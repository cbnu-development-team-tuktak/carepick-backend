package com.callrapport.component.client

import com.callrapport.model.user.OauthProvider // OAuth 제공자 enum
import com.callrapport.component.client.OauthProviderClient // OAuth 제공자 클라이언트 인터페이스
import com.callrapport.dto.response.OauthUserInfoResponse // OAuth 사용자 정보 응답 DTO
import org.springframework.stereotype.Component // Spring 컴포넌트 어노테이션
import java.util.* // Java의 UUID 클래스
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper // Jackson 라이브러리의 Kotlin 확장 기능
import com.fasterxml.jackson.module.kotlin.readValue // Jackson 라이브러리의 Kotlin 확장 기능

@Component // Spring 컴포넌트로 등록
class AppleOauthClient : OauthProviderClient {
    override fun getUserInfo(accessToken: String): OauthUserInfoResponse {
        // 액세스 토큰을 "."으로 분리하여 JWT의 페이로드 부분을 가져옴
        val parts = accessToken.split(".")
        if (parts.size != 3) {
            throw IllegalArgumentException("Invalid access token format")
        }

        val payload = String(Base64.getDecoder().decode(parts[1]))
        val claims: AppleIdTokenClaims = jacksonObjectMapper().readValue(payload)

        return OauthUserInfoResponse(
            provider = OauthProvider.APPLE, // OAuth 제공자 설정
            providerUserId = claims.sub, // Apple OAuth에서 사용자 ID는 'sub' 필드에 저장됨
            email = claims.email, // Apple OAuth에서 이메일은 'email' 필드에 저장됨
            nickname = claims.email, // Apple OAuth에서는 이메일을 닉네임으로 사용
            profileImageUrl = null // Apple OAuth에서는 프로필 이미지 URL을 제공하지 않음
        )
    }

    data class AppleIdTokenClaims(
        val sub: String, // 사용자 ID
        val email: String? // 이메일 (null일 수 있음)
    ) // Apple OAuth에서 사용하는 ID 토큰의 클레임을 나타내는 데이터 클래스
}

