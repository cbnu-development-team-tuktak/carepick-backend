package com.callrapport.component.client

import com.callrapport.model.user.OauthProvider // OAuth 제공자 enum
import com.callrapport.component.client.OauthProviderClient // OAuth 제공자 클라이언트 인터페이스
import com.callrapport.dto.response.OauthUserInfoResponse // OauthUserInfoResponse DTO 임포트
import org.springframework.stereotype.Component // 컴포넌트 어노테이션
import org.springframework.web.reactive.function.client.WebClient // WebClient 임포트

@Component
class KakaoOauthClient : OauthProviderClient {
    private val webClient = WebClient.create() // 기본 WebClient 생성

    override fun getUserInfo(accessToken: String): OauthUserInfoResponse {
        val response = webClient.get()
            .uri("https://kapi.kakao.com/v2/user/me") // 카카오 API 사용자 정보 조회 URI
            .headers { it.setBearerAuth(accessToken) } // Bearer 토큰 설정
            .retrieve() // 응답을 받아옴
            .bodyToMono(KakaoUserInfoResponse::class.java) // 응답 본문을 KakaoUserInfoResponse로 변환
            .block() ?: throw RuntimeException("Failed to retrieve user info") // 응답이 없을 경우 예외 발생

        return OauthUserInfoResponse(
            provider = OauthProvider.KAKAO, // OAuth 제공자 설정
            providerUserId = response.id.toString(), // 사용자 ID를 문자열로 변환
            email = response.kakaoAccount.email, // 이메일 정보
            nickname = response.kakaoAccount.profile.nickname, // 닉네임 정보
            profileImageUrl = response.kakaoAccount.profile.profileImageUrl // 프로필 이미지 URL 정보
        )
    }

    // 카카오 API 응답 DTO 클래스
    data class KakaoUserInfoResponse(
        val id: Long, // 카카오 사용자 ID
        val kakaoAccount: KakaoAccount // 카카오 계정 정보
    )
 
    // 카카오 계정 정보 DTO 클래스
    data class KakaoAccount(
        val email: String, // 이메일 정보 
        val profile: KakaoProfile // 프로필 정보
    )

    // 카카오 프로필 정보 DTO 클래스
    data class KakaoProfile(
        val nickname: String, // 닉네임 정보
        val profileImageUrl: String? // 프로필 이미지 URL 정보
    )
}