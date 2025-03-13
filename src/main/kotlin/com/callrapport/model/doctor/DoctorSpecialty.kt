package com.callrapport.model.doctor

// JPA 관련 import
import jakarta.persistence.*

// `Doctor` 및 `Specialty` 엔티티 import
import com.callrapport.model.common.Specialty
import com.fasterxml.jackson.annotation.JsonBackReference
import com.callrapport.model.doctor.Doctor

@Entity
@Table(name = "doctor_specialties")
data class DoctorSpecialty(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false, referencedColumnName = "id")
    @JsonBackReference // ✅ 역직렬화 방지 (순환 참조 해결)
    val doctor: Doctor, 

    @ManyToOne
    @JoinColumn(name = "specialty_id", nullable = false, referencedColumnName = "id")
    val specialty: Specialty 
)
