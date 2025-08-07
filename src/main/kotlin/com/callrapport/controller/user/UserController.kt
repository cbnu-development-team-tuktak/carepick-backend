package com.callrapport.controller.user

import com.callrapport.dto.request.RegistrationDetailsRequest // 등록 요청 DTO
import com.callrapport.dto.request.UserUpdateRequest // 사용자 업데이트 요청 DTO
import com.callrapport.dto.request.ProfileUpdateRequest // 프로필 업데이트 요청 DTO
import com.callrapport.dto.request.AddressUpdateRequest // 주소 업데이트 요청 DTO

import com.callrapport.model.user.OauthProvider // OAuth 제공자 enum
import com.callrapport.dto.response.OauthUserInfoResponse // OAuth 사용자 정보 응답 DTO
import com.callrapport.component.client.OauthClientFactory // OAuth 클라이언트 팩토리
import com.callrapport.component.client.OauthProviderClient // OAuth 제공자 클라이언트 인터페이스
import com.callrapport.service.user.UserService // 사용자 서비스
import com.callrapport.dto.response.JwtToken // JWT 토큰 응답 DTO

import org.springframework.web.bind.annotation.* // 스프링 웹 어노테이션
import org.springframework.http.ResponseEntity // HTTP 응답 엔티티
import org.springframework.http.HttpStatus // HTTP 상태 코드

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService, // 사용자 서비스 주입
    private val oauthClientFactory: OauthClientFactory // OAuth 클라이언트 팩토리 주입
) {
    // Oauth 사용자 정보 조회
    @GetMapping("/oauth-info")
    fun getOauthUserInfo(
        @RequestParam provider: OauthProvider, // OAuth 제공자 파라미터
        @RequestParam accessToken: String // 액세스 토큰 파라미터
    ): OauthUserInfoResponse {
        val client = oauthClientFactory.getClient(provider) // OAuth 제공자에 맞는 클라이언트 가져오기
        return client.getUserInfo(accessToken) // 사용자 정보 조회
    }

    // 회원가입 및 JWT 발급
    @PostMapping("/register")
    fun registerWithOauth(
        @RequestBody request: RegistrationDetailsRequest // 회원가입 요청 DTO
    ): JwtToken {
        return userService.registerWithOauth(request) // 사용자 등록 및 JWT 토큰 발급
    }

    // 닉네임, 비밀번호 수정
    @PatchMapping("/me")
    fun updateUser(@RequestBody request: UserUpdateRequest): ResponseEntity<String> {
        return try {
            val updated = userService.updateUser(request) // 사용자 정보 업데이트
            if (updated) {
                ResponseEntity.ok("User updated successfully") // 성공 응답
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found") // 사용자 없음 응답
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred") // 서버 오류 응답
        }
    }

    @PatchMapping("/me/profile")
    fun updateProfile(@RequestBody request: ProfileUpdateRequest): ResponseEntity<String> {
        return try {
            val updated = userService.updateProfile(request) // 프로필 정보 업데이트
            if (updated) {
                ResponseEntity.ok("Profile updated successfully") // 성공 응답
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("Profile not found") // 프로필 없음 응답
            }
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body("An error occurred") // 서버 오류 응답
        }
    }

    @PatchMapping("/me/address")
    fun updateAddress(@RequestBody request: AddressUpdateRequest): ResponseEntity<String> {
        return try {
            val updated = userService.updateAddress(request)
            if (updated) {
                ResponseEntity.ok("Address updated successfully") // 성공 응답
            } else {
                ResponseEntity.badRequest().body("Address not found or update failed") // 주소 없음 또는 업데이트 실패 응답
            }
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body("An error occurred") // 서버 오류 응답
        }
    }
}
