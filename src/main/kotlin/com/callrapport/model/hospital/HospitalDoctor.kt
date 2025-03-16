package com.callrapport.model.hospital

// 엔티티 관련 import
import com.callrapport.model.doctor.Doctor // Doctor: 의사 정보를 저장하는 엔티티

// JPA 관련 import
import jakarta.persistence.* // JPA 매핑을 위한 어노테이션 포함

@Entity
@Table(name = "hospital_doctors")
data class HospitalDoctor(
    @Id // 기본 키(Primary Key) 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID 값을 자동 증가(Auto Increment)하도록 설정
    val id: Long? = null, // 병원-의사 관계 ID (자동 생성됨)

    // 병원(Hospital)과 다대일(N:1) 관계
    @ManyToOne // 여러 명의 의사가 하나의 병원과 연결될 수 있음
    @JoinColumn(
        name = "hospital_id", // 매핑할 외래 키(FK) 이름 
        nullable = false, // Hospital 엔티티의 "id" 컬럼을 참조
        referencedColumnName = "id" // 필수 입력 값 (NULL 허용 안 함)
    ) 
    val hospital: Hospital, // 해당 의사가 소속된 병원

    // 의사(Doctor)와 다대일(N:1) 관계
    @ManyToOne // 여러 개의 병원이 하나의 의사와 연결될 수 있음 
    @JoinColumn(
        name = "doctor_id", // 매핑할 외래 키(FK) 이름
        nullable = false, // Docotr 엔티티의 "id" 컬럼을 참조
        referencedColumnName = "id" // 필수 입력 값 (NULL 허용 안 함)
    ) 
    val doctor: Doctor // 해당 병원에 소속된 의사
)