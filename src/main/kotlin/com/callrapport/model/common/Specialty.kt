package com.callrapport.model.common

// JPA 관련 import 
import jakarta.persistence.* // JPA 엔티티 매핑을 위한 어노테이션 포함

@Entity
@Table(name = "specialties") 
data class Specialty(
    @Id // 기본 키
    @Column(length = 10) // 최대 길이 제한
    var id: String, // 진료과 ID (예: PF000, PM000)

    @Column(
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        unique = true // 동일한 진료과 이름 중복 저장 방지
    )
    val name: String // 진료과 이름 (내과, 정형외과 등)
)
