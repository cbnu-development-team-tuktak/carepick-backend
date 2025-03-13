package com.callrapport.model.hospital

import com.callrapport.model.doctor.Doctor
import jakarta.persistence.*

@Entity
@Table(name = "hospital_doctors")
data class HospitalDoctor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "hospital_id", nullable = false, referencedColumnName = "id") // 병원의 id와 연결
    val hospital: Hospital, // 병원

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false, referencedColumnName = "id") // 의사의 id와 연결
    val doctor: Doctor // 의사
)