package com.callrapport.model.disease

// JPA 관련 import
import jakarta.persistence.* // JPA 매핑 어노테이션

import com.callrapport.model.disease.DiseaseSymptom

@Entity
@Table(name = "symptoms")
data class Symptom(
    @Id // 기본 키(Primary Key) 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 ID
    val id: Long = 0L, // 증상 고유 ID

    @Column(nullable = false, unique = true, length = 50) // 필수, 중복 방지
    val name: String, // 증상명 (예: 기침, 발열 등)

    // 질병과 증상의 관계
    @OneToMany(
        mappedBy = "symptom", // DiseaseSymptom 엔티티에서 symptom 필드를 기준으로 관계 설정
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    val diseaseSymptoms: List<DiseaseSymptom> = emptyList() // 해당 증상과 연결된 질병 목록
)
