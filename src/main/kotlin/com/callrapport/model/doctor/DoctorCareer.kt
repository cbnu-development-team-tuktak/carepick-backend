package com.callrapport.model.doctor

import jakarta.persistence.*
import com.fasterxml.jackson.annotation.JsonIgnore

@Entity
@Table(name = "doctor_careers")
data class DoctorCareer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null, 

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false, referencedColumnName = "id")
    @JsonIgnore
    val doctor: Doctor,

    @ManyToOne
    @JoinColumn(name = "career_id", nullable = false, referencedColumnName = "id")
    @JsonIgnore
    val career: Career
)