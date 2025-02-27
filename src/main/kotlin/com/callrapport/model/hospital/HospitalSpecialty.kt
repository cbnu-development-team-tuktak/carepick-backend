package com.callrapport.model.hospital

// Model (엔티티) 관련 import
import com.callrapport.model.common.Specialty // Specialty 엔티티: 의사의 진료과 정보를 저장하는 엔티티

// JPA 관련 import 
import jakarta.persistence.* 

@Entity
@Table(name = "hospital_specialties")
data class HospitalSpecialty(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long ?= null, // 기본 키

    @ManyToOne
    @JoinColumn(name = "hospital_id", nullable = false)
    val hospital: Hospital, // 병원 ID

    @ManyToOne
    @JoinColumn(name = "specialty_id", nullable = false)
    val specialty: Specialty // 진료과 ID 
)