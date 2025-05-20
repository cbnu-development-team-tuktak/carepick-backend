package com.callrapport.model.university

import jakarta.persistence.*

@Entity
@Table(name = "regions")
data class Region(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val name: String
)
