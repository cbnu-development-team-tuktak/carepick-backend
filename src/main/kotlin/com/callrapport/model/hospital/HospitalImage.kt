package com.callrapport.model.hospital

import com.callrapport.model.hospital.Hospital
import com.callrapport.model.common.Image
// JPA 관련 import
import jakarta.persistence.* // JPA 매핑을 위한 어노테이션 포함

@Entity
@Table(
    name = "hospital_images"
    uniqueConstraints = [
        UniqueConstraint(name = "UK_hospital_image", columnNames = ["hospital_id", "image_id"]) // ✅ 병원-이미지 관계 중복 방지
    ]
)
data class HospitalImage(
    @Id // 기본 키(Primary Key) 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID 값을 자동 증가(Auto Increment)하도록 설정
    val id: Long? = null, // 병원 이미지 ID (자동 생성됨)

    // 병원(Hospital)과 다대일(N:1) 관계
    @ManyToOne // 여러 이미지가 하나의 병원과 연결될 수 있음
    @JoinColumn(
        name = "hospital_id", // 매핑할 외래 키(FK) 이름
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        referencedColumnName = "id" // Hospital 엔티티의 "id" 컬럼을 참조
    )
    val hospital: Hospital, // 이미지가 속한 병원 엔티티

    // 이미지(Image)와 다대일(N:1) 관계
    @ManyToOne
    @JoinColumn(
        name = "image_id", // 매핑할 외래 키(FK) 이름
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        referencedColumnName = "id" // Image 엔티티의 "id" 컬럼을 참조
    )
    val image: Image // 해당 병원에 연결된 이미지
)