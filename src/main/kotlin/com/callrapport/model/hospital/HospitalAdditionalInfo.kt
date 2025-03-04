package com.callrapport.model.hospital

import jakarta.persistence.*
import com.fasterxml.jackson.annotation.JsonBackReference

@Entity
@Table(name = "hospital_additional_info")
data class HospitalAdditionalInfo(
    @Id
    val id: String, // 병원 ID를 id로 설정

    @OneToOne
    @JoinColumn(name = "hospital_id", referencedColumnName = "id")
    @JsonBackReference  // 순환 참조 방지를 위한 설정
    val hospital: Hospital? = null, // 병원 엔티티와 연결 (양방향 관계)

    @OneToOne
    @JoinColumn(name = "additional_info_id", referencedColumnName = "id")
    val additionalInfo: AdditionalInfo? = null // AdditionalInfo와 연결
)
