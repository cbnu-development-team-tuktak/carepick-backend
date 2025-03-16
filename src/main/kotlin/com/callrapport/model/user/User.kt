package com.callrapport.model.user

// JPA 관련 import
import jakarta.persistence.* // JPA 매핑을 위한 어노테이션 포함

// Java 날씨 및 시간 관련 import 
import java.time.LocalDate // 생년월일(localDate) 저장을 위한 클래스
import java.time.LocalDateTime // 생성 및 업데이트 날짜(LocalDateTime)을 위한 클래스

@Entity
@Table(name = "users")
data class User(
    @Id // 기본 키(Primary Key) 설정
    val id: String, // 유저 ID

    @Column(nullable =  false) // 필수 입력 값 (NULL 허용 안 함)
    val password: String, // 비밀번호

    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val name: String, // 유저 이름

    @Enumerated(EnumType.STRING) // Enum 타입을 문자열(String)로 저장
    @Column(nullable = true) // 선택적 입력 값 (NULL 허용)
    val gender: Gender? = null, // 성별 (MALE, FEMALE, OTHER)

    @Column(nullable = true) // 선택적 입력 값 (NULL 허용)
    val birthDate: LocalDate? = null, // 생년월일

    @Enumerated(EnumType.STRING) // Enum 타입을 문자열(String)로 저장
    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val status: UserStatus = UserStatus.ACTIVE, // 사용자 상태 (기본값: ACTIVE)

    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val createdAt: LocalDateTime = LocalDateTime.now(), // 계정 생성일 (기본값: 현재 시간)

    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    var updatedAt: LocalDateTime = LocalDateTime.now() // 계정 정보 수정일 (기본값: 현재 시간)
) {
    // 사용자 정보 업데이트 시, updateAt 필드를 현재 시간으로 변경
    fun updateTimestamp() {
        updatedAt = LocalDateTime.now() // 계정 정보 변경 시 자동으로 업데이트
    }
}

// 성별(Gender) Enum 정의
enum class Gender {
    MALE, // 남성
    FEMALE, // 여성
    OTHER // 기타 성별
}

// 사용자 상태(UserStatus) Enum 정의
enum class UserStatus {
    ACTIVE, // 활성 계정
    SUSPENDED, // 정지된 계정
    DEACTIVATED // 비활성화된 계정
}