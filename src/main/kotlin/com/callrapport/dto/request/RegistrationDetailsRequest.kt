package com.callrapport.dto.request

import com.callrapport.model.user.Gender
import com.callrapport.model.user.OauthProvider
import java.time.LocalDate

data class RegistrationDetailsRequest(
    // User 관련
    val userId: String, // 로그인용 ID
    val email: String, // 이메일 주소
    val password: String, // 비밀번호 (암호화된 형태로 저장)
    val nickname: String, // 닉네임

    // Oauth 관련
    val oauthProvider: OauthProvider, // OAuth 제공자
    val providerUserId: String, // OAuth 제공자의 사용자 ID
    val profileImageUrl: String? = null, // 프로필 이미지 URL (선택적)

    // Profile 관련
    val realName: String, // 실제 이름
    val birthDate: LocalDate, // 생년월일
    val gender: Gender, // 성별

    // 주소 관련
    val sidoCode: String, // 시도 코드
    val sggCode: String, // 시군구 코드
    val umdCode: String, // 읍면동 코드
    val detailAddress: String // 상세 주소
)