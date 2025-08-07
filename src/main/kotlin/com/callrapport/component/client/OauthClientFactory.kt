package com.callrapport.component.client

import com.callrapport.model.user.OauthProvider // OAuth 제공자 enum
import org.springframework.stereotype.Component // 컴포넌트 어노테이션

@Component
class OauthClientFactory(
    private val kakaoOauthClient: KakaoOauthClient, // 카카오 OAuth 클라이언트
    private val googleOauthClient: GoogleOauthClient, // 구글 OAuth 클라이언트
    private val naverOauthClient: NaverOauthClient, // 네이버 OAuth 클라이언트
    private val appleOauthClient: AppleOauthClient // 애플 OAuth 클라이언트
) {
    fun getClient(provider: OauthProvider): OauthProviderClient {
        return when (provider) {
            OauthProvider.KAKAO -> kakaoOauthClient // 카카오 OAuth 클라이언트 반환
            OauthProvider.GOOGLE -> googleOauthClient // 구글 OAuth 클라이언트 반환
            OauthProvider.NAVER -> naverOauthClient // 네이버 OAuth 클라이언트 반환
            OauthProvider.APPLE -> appleOauthClient // 애플 OAuth 클라이언트 반환
        }
    }
}