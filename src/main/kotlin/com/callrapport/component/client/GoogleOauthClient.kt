package com.callrapport.component.client

import com.callrapport.model.user.OauthProvider // OAuth 제공자 enum
import com.callrapport.component.client.OauthProviderClient // OAuth 제공자 클라이언트 인터페이스
import com.callrapport.dto.response.OauthUserInfoResponse // OauthUserInfoResponse DTO 임포트
import org.springframework.stereotype.Component // 컴포넌트 어노테이션
import org.springframework.web.reactive.function.client.WebClient // WebClient 임포트

@Component
class GoogleOauthClient : OauthProviderClient {
    private val webClient = WebClient.create() // 기본 WebClient 생성

    override fun getUserInfo(accessToken: String): OauthUserInfoResponse {
        val response = webClient.get()
            .uri("https://www.googleapis.com/oauth2/v3/userinfo")
            .headers { it.setBearerAuth(accessToken) } // Bearer 토큰 설정
            .retrieve() // 응답을 받아옴
            .bodyToMono(GoogleUserInfoResponse::class.java) // 응답 본문을 OauthUserInfoResponse로 변환
            .block() ?: throw IllegalArgumentException("Failed to retrieve user info") // 응답이 없을 경우 예외 발생

        return OauthUserInfoResponse(
            provider = OauthProvider.GOOGLE, // OAuth 제공자 설정
            providerUserId = response.sub, // 사용자 ID
            email = response.email, // 이메일 정보
            nickname = response.name, // 이름 정보
            profileImageUrl = response.picture // 프로필 이미지 URL 정보
        )
    }

    // 구글 사용자 정보 응답 DTO 클래스
    data class GoogleUserInfoResponse(
        val sub: String, // 구글 사용자 ID
        val email: String, // 이메일 정보
        val name: String, // 이름 정보
        val picture: String // 프로필 이미지 URL 정보
    )
}
