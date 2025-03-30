package com.callrapport.model.disease

// 엔티티 관련 import
import com.callrapport.model.doctor.Doctor // Doctor: 의사 정보를 저장하는 엔티티
import com.callrapport.model.disease.Disease // Disease: 정제된 질병 정보를 저장하는 엔티티

// JPA 관련 import
import jakarta.persistence.* // JPA 매핑을 위한 어노테이션 포함

@Entity
@Table(name = "disease_doctor")
data class DiseaseDoctor(
    @Id // 기본 키(Primary Key) 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID 값을 자동 증가(Auto Increment)하도록 설정
    val id: Long = 0L, // 연결 엔티티의 고유 ID

    @ManyToOne // 질병(Disease)과 다대일(N:1) 관계
    @JoinColumn(
        name = "disease_id", // 매핑할 외래 키(FK) 이름
        referencedColumnName = "id", // Disease 엔티티의 "id" 컬럼을 참조
        nullable = false // 필수 입력 값 (NULL 허용 안 함)
    )
    val disease: Disease, // 해당 질병을 잘 진료하는 의사가 연결될 질병 정보

    @ManyToOne // 의사(Doctor)와 다대일(N:1) 관계
    @JoinColumn(
        name = "doctor_id", // 매핑할 외래 키(FK) 이름
        referencedColumnName = "id", // Doctor 엔티티의 "id" 컬럼을 참조
        nullable = false // 필수 입력 값 (NULL 허용 안 함)
    )
    val doctor: Doctor // 해당 질병을 진료할 수 있는 관련 의사 정보
)