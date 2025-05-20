package com.callrapport.model.university

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    // 대학과 다대일 관계 (N:1)
    @ManyToOne
    @JoinColumn(
        name = "university_rank_id",
        nullable = false,
        referencedColumnName = "id"
    )
    @JsonIgnore
    val universityRank: UniversityRank,

    // 지역과 다대일 관계 (N:1)
    @ManyToOne
    @JoinColumn(
        name = "region_id",
        nullable = false,
        referencedColumnName = "id"
    )
    @JsonIgnore
    val region: Region
)
