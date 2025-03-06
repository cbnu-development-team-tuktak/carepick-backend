package com.callrapport.model.user

import com.callrapport.model.hospital.Hospital
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user_favorite_hospitals")
data class UserFavortieHospital(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    val user: User,

    @ManyToOne
    @JoinColumn(name = "hospital_id", referencedColumnName = "id", nullable = false)
    val hospital: Hospital,

    @Column(nullable = false)
    val createdAT: LocalDateTime = LocalDateTime.now() // 즐겨찾기 추가 시간
)