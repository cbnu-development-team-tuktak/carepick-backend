package com.callrapport.service.user

import com.callrapport.dto.request.RegistrationDetailsRequest // 등록 요청 DTO
import com.callrapport.dto.request.UserUpdateRequest // 사용자 업데이트 요청 DTO
import com.callrapport.dto.request.ProfileUpdateRequest // 프로필 업데이트 요청 DTO
import com.callrapport.dto.request.AddressUpdateRequest // 주소 업데이트 요청 DTO

import com.callrapport.model.user.Oauth // OAuth 관련 모델 
import com.callrapport.model.user.User // 사용자 모델
import com.callrapport.model.user.Profile // 프로필 모델
import com.callrapport.model.user.UserOauth // 사용자 OAuth 정보 모델
import com.callrapport.model.user.Gender // 성별 열거형
import com.callrapport.model.user.ProfileAddress // 프로필 주소 모델
import com.callrapport.model.user.UserProfile // 사용자 프로필 모델
import com.callrapport.model.user.Address // 주소 모델

import com.callrapport.repository.user.UserRepository // 사용자 관련 레포지토리
import com.callrapport.repository.user.OauthRepository // OAuth 관련 레포지토리
import com.callrapport.repository.user.UserProfileRepository // 사용자 프로필 관련 레포지토리
import com.callrapport.repository.user.UserOauthRepository // 사용자 OAuth 정보 관련 레포지토리
import com.callrapport.repository.user.AddressRepository // 주소 관련 레포지토리
import com.callrapport.repository.administrativeRegion.SidoRepository // 시도 관련 레포지토리
import com.callrapport.repository.administrativeRegion.SggRepository // 시군구 관련 레포지토리
import com.callrapport.repository.administrativeRegion.UmdRepository // 읍면동 관련 레포지토리
import com.callrapport.repository.user.ProfileRepository // 프로필 관련 레포지토리
import com.callrapport.repository.user.ProfileAddressRepository // 프로필 주소 관련 레포지토리

import com.callrapport.component.client.OauthClientFactory // OAuth 클라이언트 팩토리
import com.callrapport.component.jwt.JwtTokenProvider // JWT 토큰 생성기

import com.callrapport.dto.response.JwtToken // JWT 토큰 응답 DTO

import jakarta.transaction.Transactional // 트랜잭션 관리
import org.springframework.stereotype.Service // 서비스 어노테이션

@Service
class UserService(
    private val userRepository: UserRepository, // 사용자 관련 레포지토리
    private val oauthRepository: OauthRepository, // OAuth 관련 레포지토리
    private val userProfileRepository: UserProfileRepository, // 사용자 프로필 관련 레포지토리
    private val userOauthRepository: UserOauthRepository, // 사용자 OAuth 정보 관련 레포지토리
    private val addressRepository: AddressRepository, // 주소 관련 레포지토리
    private val sidoRepository: SidoRepository, // 시도 관련 레포지토리
    private val sggRepository: SggRepository, // 시군구 관련 레포지토리
    private val umdRepository: UmdRepository, // 읍면동 관련 레포지토리
    private val profileRepository: ProfileRepository, // 프로필 관련 레포지토리
    private val profileAddressRepository: ProfileAddressRepository, // 프로필 주소 관련 레포지토리
    private val oauthClientFactory: OauthClientFactory, // OAuth 클라이언트 팩토리
    private val jwtTokenProvider: JwtTokenProvider // JWT 토큰 생성기
) {
    // OAuth 사용자 등록 및 JWT 발급
    fun registerWithOauth(request: RegistrationDetailsRequest): JwtToken {
        // OAuth 제공자에 해당하는 클라이언트 가져오기
        val existing = oauthRepository.findByProviderAndProviderUserId(
            request.oauthProvider, // OAuth 제공자와 사용자 ID로 조회
            request.providerUserId // OAuth 제공자의 사용자 ID
        )

        if (existing != null) {
            throw IllegalArgumentException("User with this Oauth provider and user ID already exists")
        }

        val userId = register(request) // 사용자 등록

        val user = userRepository.findById(userId).orElseThrow { 
            IllegalArgumentException("User not found with ID: $userId") 
        }

        return jwtTokenProvider.generateToken(user.id!!.toString())
    }

    // 회원가입 처리
    @Transactional
    fun register(request: RegistrationDetailsRequest): Long {
        // Address 생성
        val sido = sidoRepository.findByCode(request.sidoCode) 
            ?: throw IllegalArgumentException("Invalid sido code: ${request.sidoCode}") 
        val sgg = sggRepository.findByCode(request.sggCode)
            ?: throw IllegalArgumentException("Invalid sgg code: ${request.sggCode}")
        val umd = umdRepository.findByCode(request.umdCode)
            ?: throw IllegalArgumentException("Invalid umd code: ${request.umdCode}")
         
        val address = addressRepository.save(
            Address(
                sido = sido, // 시도
                sgg = sgg, // 시군구
                umd = umd, // 읍면동
                detailAddress = request.detailAddress // 상세 주소
            )
        )
        
        // Profile 저장 
        val profile = profileRepository.save(
            Profile(
                realName = request.realName, // 실명
                birthDate = request.birthDate, // 생년월일
                gender = request.gender // 성별
            )
        )

        // ProfileAddress 저장
        val profileAddress = profileAddressRepository.save(
            ProfileAddress(
                profile = profile, // 프로필
                address = address // 주소
            )
        )

        // User 저장
        val user = userRepository.save(
            User(
                userId = request.userId, // 로그인용 ID
                email = request.email, // 이메일 주소
                password = request.password, // 비밀번호 (암호화된 형태로 저장)
                nickname = request.nickname // 닉네임
            )
        )

        // UserProfile 저장
        userProfileRepository.save(
            UserProfile(
                user = user, // 연결된 사용자
                profile = profile // 연결된 프로필
            )
        )

        // Oauth 저장
        val oauth =  oauthRepository.save(
            Oauth(
                provider = request.oauthProvider, // OAuth 제공자
                providerUserId = request.providerUserId, // OAuth 제공자의 사용자 ID
                email = request.email, // 이메일 주소
                nickname = request.nickname, // 닉네임
                profileImageUrl = request.profileImageUrl // 프로필 이미지 URL
            )
        )

        // UserOauth 저장
        userOauthRepository.save(
            UserOauth(
                user = user, // 연결된 사용자
                oauth = oauth // 연결된 OAuth 정보
            )
        )

        return user.id ?: throw IllegalArgumentException("User registration failed")
    }

    // 사용자 정보 업데이트
    fun updateUser(request: UserUpdateRequest): Boolean {
        val user = userRepository.findByUserId(request.userId) // 사용자 ID로 사용자 조회
            ?: return false
        
        val updatedUser = user.copy(
            nickname = request.nickname ?: user.nickname, // 닉네임 업데이트
            password = request.password ?: user.password // 비밀번호 업데이트 (null이면 기존 비밀번호 유지)
        )

        userRepository.save(updatedUser) // 업데이트된 사용자 정보 저장
        return true
    }

    fun updateProfile(request: ProfileUpdateRequest): Boolean {
        val user = userRepository.findByUserId(request.userId) // 사용자 ID로 사용자 조회
            ?: return false
        
        val profile = user.userProfile?.profile
            ?: return false // 사용자 프로필이 없으면 false 반환

        val updatedProfile = profile.copy(
            realName = request.realName ?: profile.realName, // 실명 업데이트
            birthDate = request.birthDate ?: profile.birthDate, // 생년월일 업데이트
            gender = request.gender ?: profile.gender // 성별 업데이트
        )

        profileRepository.save(updatedProfile) // 업데이트된 프로필 정보 저장
        return true
    }

    fun updateAddress(request: AddressUpdateRequest): Boolean {
        val user = userRepository.findByUserId(request.userId)
            ?: return false // 사용자 ID로 사용자 조회 실패 시 false 반환
        
        val profile = user.userProfile?.profile
            ?: return false // 사용자 프로필이 없으면 false 반환
        
        val profileAddress = profileAddressRepository.findByProfile(profile)
            ?: return false // 프로필 주소가 없으면 false 반환
        
        val sido = sidoRepository.findByCode(request.sidoCode)
            ?: return false // 시도 코드로 시도 조회 실패 시 false 반환
        val sgg = sggRepository.findByCode(request.sggCode)
            ?: return false // 시군구 코드로 시군구 조회 실패 시 false 반환
        val umd = umdRepository.findByCode(request.umdCode)
            ?: return false // 읍면동 코드로 읍면동 조회 실패 시 false 반환
        
        val updatedAddress = profileAddress.address.copy(
            sido = sido, // 시도 업데이트
            sgg = sgg, // 시군구 업데이트
            umd = umd, // 읍면동 업데이트
            detailAddress = request.detailAddress ?: profileAddress.address.detailAddress // 상세 주소 업데이트
        )

        addressRepository.save(updatedAddress) // 업데이트된 주소 정보 저장
        return true // 업데이트 성공 시 true 반환
    }       
}