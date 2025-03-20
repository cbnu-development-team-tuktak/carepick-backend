package com.callrapport.model.hospital

// 엔티티 관련 import
import com.callrapport.model.common.Specialty // Specialty: 의사의 진료과 정보를 저장하는 엔티티

// JPA 관련 import 
import jakarta.persistence.* 

// JSON 직렬화 관련 import
import com.fasterxml.jackson.annotation.JsonBackReference // 순환 참조 방지를 위한 JSON 어노테이션 

@Entity
@Table(
    name = "hospital_specialties",
    uniqueConstraints = [ // 병원 ID + 진료과 ID 조합이 중복되지 않도록 설정
        UniqueConstraint(columnNames = ["hospital_id", "specialty_id"])
    ]
)
data class HospitalSpecialty(
    @Id // 기본 키(Primary Key) 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID 값을 자동 증가(Auto Increment)하도록 설정
    val id: Long ?= null, // 병원-진료과 관계 ID (자동 생성됨)

    // 병원(Hospital)과 다대일(N:1) 관계
    @ManyToOne // 여러 개의 HospitalSpecialty가 하나의 병원과 연결될 수 있음
    @JoinColumn(
        name = "hospital_id", // 매핑할 외래 키(FK) 이름
        nullable = false // 필수 입력 값 (NULL 허용 안 함)
    )
    @JsonBackReference // 순환 참조 방지
    val hospital: Hospital, // 병원 엔티티와 연결 (N:1 관계)

    // 진료과(Specialty)와 다대일(N:1) 관계
    @ManyToOne // 여러 개의 HospitalSpecialty가 하나의 Specialty와 연결될 수 있음
    @JoinColumn(
        name = "specialty_id", // 매핑할 외래 키(FK) 이름
        nullable = false // 필수 입력 값 (NULL 허용 안 함)
    )
    val specialty: Specialty // 병원이 제공하는 진료과 정보 (N:1 관계)
)