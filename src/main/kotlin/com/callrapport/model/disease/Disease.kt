package com.callrapport.model.disease

// JSON 직렬화 관련 import
import com.fasterxml.jackson.annotation.JsonManagedReference // 순환 참조 방지를 위해 사용

// 엔티티 관련 import
import com.callrapport.model.common.Specialty // Specialty: 질병의 진료과 정보를 저장하는 엔티티

// JPA 관련 import 
import jakarta.persistence.*

@Entity
@Table(name = "diseases")
data class Disease(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 ID
    val id: Long = 0L, // 질병 ID (PK)

    @Column(
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        unique = true
    )
    val name: String, // 질병명

    @OneToMany(
        mappedBy = "disease", // DiseaseCategory 엔티티의 'disease' 필드와 매핑
        cascade = [CascadeType.ALL], // Disease 삭제 시 관련 데이터도 삭제됨
        orphanRemoval = true // 고아 객체 자동 삭제
    )
    @JsonManagedReference
    val diseaseCategory: MutableList<DiseaseCategory> = mutableListOf(), // 질병-분류 관계

    @OneToMany(
        mappedBy = "disease", // DiseaseBodySystem 엔티티의 'disease' 필드와 매핑
        cascade = [CascadeType.ALL], // Disease 삭제 시 관련 데이터도 삭제됨
        orphanRemoval = true // 고아 객체 자동 삭제
    )
    @JsonManagedReference
    val diseaseBodySystem: MutableList<DiseaseBodySystem> = mutableListOf(), // 질병-신체계통 관계

    @OneToMany(
        mappedBy = "disease", // DiseaseSpecialty 엔티티의 disease 필드와 매핑
        cascade = [CascadeType.ALL], // Disease 삭제 시 관련 데이터도 삭제됨
        orphanRemoval = true // 고아 객체 자동 삭제
    )
    @JsonManagedReference
    val diseaseSpecialties: MutableList<DiseaseSpecialty> = mutableListOf() // 질병을 진료할 수 있는 진료과 목록
)