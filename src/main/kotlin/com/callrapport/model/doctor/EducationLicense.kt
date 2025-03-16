package com.callrapport.model.doctor

// JPA 관련 import
import jakarta.persistence.* // JPA 엔티티 매핑을 위한 어노테이션 포함

@Entity
@Table(name = "education_licenses")
data class EducationLicense( 
    @Id // 기본 키(Primary Key) 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID 값을 자동 증가(Auto Increment)하므로 설정
    val id: Long? = null, // 자격면허 ID (자동 생성됨)

    @Column(
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        unique = true // 중복 방지
    ) 
    val name: String // 자격면허명
)
