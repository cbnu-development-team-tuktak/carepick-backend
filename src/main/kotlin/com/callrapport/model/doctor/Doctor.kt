package com.callrapport.model.doctor

import com.callrapport.model.hospital.Hospital
import jakarta.persistence.*

@Entity
@Table(name = "doctors")
class Doctor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String, // 의사 이름

    @Column(nullable = true) // ✅ nullable 설정을 올바르게 변경
    val gender: String?, // 성별 (남성/여성)

    @Column(nullable = false)
    val specialty: String, // 전문 과목

    @Column(nullable = true)
    val experience: Int?, // 경력 (년 단위)

    @Column(nullable = true, length = 1000)
    val education: String?, // 학력 정보

    @Column(nullable = true, length = 1000)
    val career: String?, // 경력 정보

    @Column(nullable = true, length = 500)
    val certifications: String?, // 자격증 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    val hospital: Hospital // 소속 병원
)
