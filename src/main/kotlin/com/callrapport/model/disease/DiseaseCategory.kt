package com.callrapport.model.disease

// JPA 관련 import
import jakarta.persistence.*

// 엔티티 관련 import
import com.callrapport.model.disease.Disease // Disease: 질병 정보를 저장하는 엔티티
import com.callrapport.model.disease.Category // Category: 질병 분류(ex. 호흡기계 질환)를 저장하는 엔티티

// JSON 직렬화 관련 import
import com.fasterxml.jackson.annotation.JsonBackReference // 순환 방지를 위한 어노테이션

@Entity
@Table(name = "disease_categories")
data class DiseaseCategory(
    @Id // 기본 키 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가
    val id: Long? = null, // 질병-분류 관계 ID

    // 질병(Disease)와 다대일(N:1) 관계
    @ManyToOne // 여러 개의 DiseaseCategory가 하나의 Disease에 연결될 수 있음
    @JoinColumn(
        name = "disease_id", // 매핑할 외래 키(FK) 이름
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        referencedColumnName = "id" // Disease 엔티티의 "id" 컬럼을 참조
    )
    @JsonBackReference // 순환 참조 방지
    val disease: Disease, // 특정 진료과에 해당하는 질병

    // 분류(Category)와 다대일(N:1) 관계
    @ManyToOne // 여러 개의 DiseaseCategory가 하나의 Category에 연결될 수 있음
    @JoinColumn(
        name = "category_id", // 매핑할 외래 키(FK) 이름
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        referencedColumnName = "id" // Category 엔티티의 "id" 컬럼을 참조
    )
    val category: Category // 해당 질병 분류
)
