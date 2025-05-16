package com.callrapport.model.disease

// JPA 관련 import
import jakarta.persistence.*

// 엔티티 관련 import
import com.callrapport.model.disease.Disease // Disease: 질병 정보를 저장하는 엔티티
import com.callrapport.model.disease.BodySystem // BodySystem: 신체 계통을 저장하는 엔티티

// JSON 직렬화 관련 import
import com.fasterxml.jackson.annotation.JsonBackReference // 순환 참조 방지를 위한 어노테이션

@Entity
@Table(name = "disease_body_systems")
data class DiseaseBodySystem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가
    val id: Long? = null, // 질병-신체계통 관게 ID

    // 질병(Disease)과 다대일(N:1) 관계
    @ManyToOne // 여러 개의 DiseaseBodySystem이 하나의 Disease에 연결될 수 있음
    @JoinColumn(
        name = "disease_id", // 매핑할 외래 키(FK) 이름
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        referencedColumnName = "id" // Disease 엔티티의 "id" 컬럼을 참조
    )
    @JsonBackReference // 순환 참조 방지
    val disease: Disease, // 특정 진료과에 해당하는 질병

    // 신체계통(BodySystem)과 다대일(N:1) 관계
    @ManyToOne // 여러 개의 DiseaseBodySystem이 하나의 BodySystem에 연결될 수 있음
    @JoinColumn(
        name = "body_system_id", // 매핑할 외래 키(FK) 이름
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        referencedColumnName = "id" // BodySystem 엔티티의 "id" 컬럼을 참조
    )
    val bodySystem: BodySystem // 해당 질병과 연관된 신체계통
)
