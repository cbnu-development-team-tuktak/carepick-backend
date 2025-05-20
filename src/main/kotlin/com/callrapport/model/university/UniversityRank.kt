package com.callrapport.model.university

import jakarta.persistence.*

@Entity
@Table(name = "university_rank")
data class UniversityRank(
    @Id
    @Column(name = "id")
    val id: Int,

    @Column(name = "kr_name", nullable = false, unique = true)
    val krName: String,

    @Column(name = "en_name", nullable = false, unique = true)
    val enName: String,

    @OneToMany(mappedBy = "universityRank", cascade = [CascadeType.ALL], orphanRemoval = true)
    val universityRankRegions: MutableList<UniversityRankRegion> = mutableListOf()
)
