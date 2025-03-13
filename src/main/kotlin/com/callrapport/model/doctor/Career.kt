package com.callrapport.model.doctor

import jakarta.persistence.*

@Entity
@Table(name = "careers")
data class Career(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(
        nullable = false,
        unique = true
    )
    val name: String
)