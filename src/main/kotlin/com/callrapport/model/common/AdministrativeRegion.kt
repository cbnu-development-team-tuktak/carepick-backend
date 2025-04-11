package com.callrapport.model.common

// JPA 관련 import
import jakarta.persistence.* // JPA 엔티티 매핑을 위한 어노테이션 포함

@Entity
@Table(name = "administrative_region")
data class AdministrativeRegion(
    @Id // 기본 키 (Primary Key) 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 (Auto Increment)
    val id: Long? = null, // 행정구역 ID

    @Column(
        name = "sido_nm", 
        nullable = false // 필수 입력 값 (NULL 허용 안 함)
    )
    val sidoNm: String, // 시도명

    @Column(
        name = "sgg_nm", 
        nullable = false // 필수 입력 값 (NULL 허용 안 함)
    )
    val sggNm: String, // 시군구명

    @Column(
        name = "umd_nm", 
        nullable = true // 선택 입력 값 (NULL 허용)
    )
    val umdNm: String?, // 읍면동명 (nullable)

    @Column(
        name = "ri_nm", 
        nullable = true // 선택 입력 값 (NULL 허용)
    )
    val riNm: String? // 리명 (nullable)
)
