package com.callrapport.model.disease

// JPA 관련 import
import jakarta.persistence.* // JPA 매핑 어노테이션

@Entity
@Table(name = "disease_symptom") // 질병-증상 매핑 테이블
data class DiseaseSymptom(
    @Id // 기본 키
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 ID
    val id: Long = 0L, // 매핑 고유 ID

    @ManyToOne // 질병(Disease)과 다대일 관계
    @JoinColumn(
        name = "disease_id", // 외래 키 이름
        referencedColumnName = "id", // Disease 엔티티의 ID 참조
        nullable = false // 필수 항목
    )
    val disease: Disease, // 연결된 질병

    @ManyToOne // 증상(Symptom)과 다대일 관계
    @JoinColumn(
        name = "symptom_id", // 외래 키 이름
        referencedColumnName = "id", // Symptom 엔티티의 ID 참조
        nullable = false // 필수 항목
    )
    val symptom: Symptom // 연결된 증상
)
