package com.callrapport.component.client

import com.callrapport.model.user.OauthProvider // OAuth 제공자 enum
import com.callrapport.component.client.OauthProviderClient // OAuth 제공자 클라이언트 인터페이스
import com.callrapport.dto.response.OauthUserInfoResponse // OAuth 사용자 정보 응답 DTO
import org.springframework.stereotype.Component // 컴포넌트 어노테이션을 사용하여 스프링 빈으로 등록
import org.springframework.web.reactive.function.client.WebClient // WebClient를 사용하여 비동기 HTTP 요청을 처리

@Component
class NaverOauthClient : OauthProviderClient {
    private val webClient = WebClient.create()

    override fun getUserInfo(accessToken: String): OauthUserInfoResponse {
        val response = webClient.get()
            .uri("https://openapi.naver.com/v1/nid/me") // 네이버 OAuth 사용자 정보 API 엔드포인트
            .headers { it.setBearerAuth(accessToken) } // Bearer 토큰을 헤더에 추가
            .retrieve() // 요청을 보내고 응답을 받음
            .bodyToMono(NaverUserInfoResponse::class.java) // 응답 본문을 NaverUserInfoResponse 타입으로 변환
            .block() ?: throw IllegalArgumentException("Failed to retrieve user info from Naver") // 응답이 null인 경우 예외 발생

        val userInfo = response.response

        return OauthUserInfoResponse(
            provider = OauthProvider.NAVER, // OAuth 제공자 설정
            providerUserId = userInfo.id,
            email = userInfo.email,
            nickname = userInfo.nickname, 
            profileImageUrl = userInfo.profileImage
        )
    }

    // 네이버 사용자 정보 응답 DTO
    data class NaverUserInfoResponse(
        val resultCode: String, // 결과 코드
        val message: String, // 메시지
        val response: NaverUser // 사용자 정보
    )

    // 네이버 사용자 정보 DTO
    data class NaverUser(
        val id: String, // 사용자 ID
        val email: String, // 이메일
        val nickname: String, // 닉네임
        val profileImage: String? // 프로필 이미지 URL (null일 수 있음)
    )
}