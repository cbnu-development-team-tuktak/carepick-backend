package com.callrapport.model.hospital

import jakarta.persistence.*

@Entity
@Table(name = "additional_info")
data class AdditionalInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,  // ID

    @Column(nullable = false)
    val open24Hours: Boolean = false, // 24시간 문의 가능 여부

    @Column(nullable = false)
    val emergencyTreatment: Boolean = false, // 24시간 응급 환자 진료 가능 여부

    @Column(nullable = false)
    val maleFemaleDoctorChoice: Boolean = false, // 남여 전문의 선택 가능 여부

    @Column(nullable = false)
    val networkHospital: Boolean = false, // 네트워크 병원 여부

    @Column(nullable = false)
    val freeCheckup: Boolean = false, // 무료 검진 제공 여부

    @Column(nullable = false)
    val nearSubway: Boolean = false, // 역세권 위치 여부

    @Column(nullable = false)
    val openAllYear: Boolean = false, // 연중무휴 진료 여부

    @Column(nullable = false)
    val openOnSunday: Boolean = false, // 일요일 & 공휴일 진료 여부

    @Column(nullable = false)
    val nightShift: Boolean = false, // 평일 야간 진료 여부

    @Column(nullable = false)
    val collaborativeCare: Boolean = false, // 협진 시스템 지원 여부

    @Column(nullable = false)
    val noLunchBreak: Boolean = false // 점심시간 없이 진료 여부
)
