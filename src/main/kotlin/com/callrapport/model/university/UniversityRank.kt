package com.callrapport.model.university

import jakarta.persistence.*

@Entity
@Table(name = "university_rank")
data class UniversityRank(
    @Id
    @Column(name = "id")
    val id: Int,  // rank 값과 동일하게 설정

    @Column(name = "kr_name", nullable = false, unique = true)
    val krName: String,

    @Column(name = "en_name", nullable = false, unique = true)
    val enName: String,

    @Column(name = "region", nullable = false)
    val region: String
)
