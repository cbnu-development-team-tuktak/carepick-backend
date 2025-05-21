package com.callrapport.model.university

// JPA 관련 import
import jakarta.persistence.* // JPA 엔티티 매핑을 위한 어노테이션 포함

// JSON 직렬화 관련 import
import com.fasterxml.jackson.annotation.JsonManagedReference // 순환 참조 방지를 위해 사용

@Entity
@Table(
    name = "university_rank_region",
    uniqueConstraints = [
        // 동일 대학-지역 중복 등록 방지
        UniqueConstraint(
            name = "UK_university_rank_region",
            columnNames = ["university_rank_id", "region_id"]
        )
    ]
)
data class UniversityRankRegion(
    @Id // 기본 키(Primary Key) 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID 값을 자동 증가(Auto Increment)하도록 설정
    val id: Long? = null, // 매핑 ID

    // 대학과 다대일 관계 (N:1)
    @ManyToOne
    @JoinColumn(
        name = "university_rank_id", // 매핑될 외래 키 컬럼명
        nullable = false, // 필수 값 (NULL 허용 안 함)
        referencedColumnName = "id" // 참조할 UniversityRank의 기본 키
    )
    @JsonIgnore // 순환 참조 방지를 위해 직렬화에서 제외
    val universityRank: UniversityRank, // 매핑된 대학 정보

    // 지역과 다대일 관계 (N:1)
    @ManyToOne
    @JoinColumn(
        name = "region_id", // 매핑될 외래 키 컬럼명
        nullable = false, // 필수 값 (NULL 허용 안 함)
        referencedColumnName = "id" // 참조할 Region의 기본 키
    )
    @JsonIgnore // 순환 참조 방지를 위해 직렬화에서 제외
    val region: Region // 매핑된 지역 정보
)
