package com.callrapport.model.common

// JPA 관련 import
import jakarta.persistence.* // JPA 엔티티 매핑을 위한 어노테이션 포함

// 날짜 및 시간 관련 import
import java.time.LocalDateTime // 생성 및 수정 시간에 사용되는 날짜/시간 클래스 

@Entity
@Table(
    name = "images",
    uniqueConstraints = [UniqueConstraint(columnNames = ["url"])] // URL 컬럼에 유니크 제약 조건 설정
)
data class Image(
    @Id // 기본 키(Primary Key) 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 (Auto Increment)
    val id: Long? = null, // 이미지 ID

    @Column(
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        unique = true, // 중복된 URL 저장 불가
        columnDefinition = "TEXT" // 매우 긴 문자열 저장
    )
    val url: String, // 이미지 url

    @Column(nullable = false, length = 64)
    val urlHash: String = "",
    
    @Column(nullable = false) // 필수 입력 값 (NULL 값 허용 안함)
    val alt: String, // 이미지 alt 텍스트
    
    @Column(nullable = false) // 필수 입력 값 (NULL 값 허용 안함)
    val createdAt: LocalDateTime = LocalDateTime.now(), // 이미지 생성 시각 (기본값: 현재 시간)

    @Column(nullable = false) // 필수 입력 값 (NULL 값 허용 안함)
    val updatedAt: LocalDateTime = LocalDateTime.now() // 이미지 수정 시각 (기본값: 현재 시간)
)