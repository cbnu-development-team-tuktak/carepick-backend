package com.callrapport.model.university

// JPA 관련 import
import jakarta.persistence.* // JPA 엔티티 매핑을 위한 어노테이션 포함

@Entity
@Table(name = "university_rank")
data class UniversityRank(
    @Id // 기본 키(Primary Key) 설정
    @Column(
        name = "id" // 컬럼명
    ) 
    val id: Int, // id = 대학 순위

    @Column(
        name = "kr_name", // 컬럼명
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        unique = true // 중복 방지
    )
    val krName: String, // 대학명 (한글)

    @Column(
        name = "en_name", // 컬럼명
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        unique = true // 중복 방지
    )
    val enName: String, // 대학명 (영문)

    // 대학과 지역 간의 관계(N:1) 매핑을 관리하는 UniversityRankRegion과 1:N 관계
    @OneToMany(
        mappedBy = "universityRank",  // UniversityRankRegion 엔티티의 universityRank 필드와 매핑
        cascade = [CascadeType.ALL], // 대학 정보 삭제 시 관련 지역 매핑 정보도 함께 삭제됨
        orphanRemoval = true // 고아 객체 자동 삭제
    )
    val universityRankRegions: MutableList<UniversityRankRegion> = mutableListOf()
)
