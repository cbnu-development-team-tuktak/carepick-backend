package com.callrapport.model.hospital

// JPA 관련 import
import jakarta.persistence.* // JPA 매핑을 위한 어노테이션 포함

// JSON 직렬화 관련 import
import com.fasterxml.jackson.annotation.JsonBackReference // 순환 참조 방지를 위한 JSON 어노테이션

@Entity
@Table(name = "hospital_additional_info")
data class HospitalAdditionalInfo(
    @Id // 기본 키(Primary Key) 설정
    val id: String, // 병원 ID를 id로 설정

    // 병원(Hospital)과 1:1 관계 (병원 추가 정보를 저장)
    @OneToOne // 하나의 Hospital이 하나의 HospitalAdditionalInfo와 연결됨
    @JoinColumn(
        name = "hospital_id", // 매핑할 외래 키(FK) 이름
        referencedColumnName = "id" // Hospital 엔티티의 "id" 컬럼을 참조
    )
    @JsonBackReference  // 순환 참조 방지를 위한 설정
    val hospital: Hospital? = null, // 병원 엔티티와 연결 (양방향 관계)

    // AdditionalInfo와 1:1 관계 (병원의 부가 정보를 저장)
    @OneToOne // 하나의 HospitalAdditionalInfo와 하나의 AdditionalIfno와 연결됨
    @JoinColumn(
        name = "additional_info_id", // 매핑할 외래 키(FK) 이름
        referencedColumnName = "id" // AdditionalInfo 엔티티의 "id" 컬럼을 참조
    )
    val additionalInfo: AdditionalInfo? = null // 병원의 부가 정보 엔티티와 연결
)
