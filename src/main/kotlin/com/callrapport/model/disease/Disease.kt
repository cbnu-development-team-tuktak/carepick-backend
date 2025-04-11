package com.callrapport.model.disease

// 엔티티 관련 import
import com.callrapport.model.disease.DiseaseDoctor // DiseaseDoctor: 질병-의사 연결 엔티티
import com.callrapport.model.disease.DiseaseSymptom // DiseaseSymptom: 질병-증상 연결 엔티티
import com.callrapport.model.disease.DiseaseSpecialty // DiseaseSpecialty: 질병-진료과 연결 엔티티

// JPA 관련 import
import jakarta.persistence.* // JPA 엔티티 매핑 및 컬럼 설정, enum 처리 등을 위한 어노테이션 제공

@Entity
@Table(name = "disease")
data class Disease(
    @Id // 기본 키(Primary Key) 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 ID
    val id: Long = 0L, // 질병 고유 ID

    @Column(nullable = false, unique = true) // 필수 입력 값, 중복 불가
    val name: String, // 질병명 (예: 폐렴, 대상포진 등)

    @Column(name = "body_system", nullable = false) // 실제 DB 컬럼명을 'body_system'으로 지정
    val bodySystem: String, // 신체계통 (예: 순환기, 피부 등)

    // 질병과 증상과의 관계 (1:N)
    @OneToMany(
        mappedBy = "disease", // DiseaseSymptom 엔티티에서 disease 필드를 기준으로 관계 설정
        cascade = [CascadeType.ALL], // 질병 삭제 시 관련 DiseaseSymptom도 함께 삭제
        orphanRemoval = true // 관계가 끊기면 DB에서 삭제됨
    )
    val diseaseSymptoms: List<DiseaseSymptom> = emptyList(), // 해당 질병과 연결된 증상 목록

    // 질병과 진료과와의 관계 (1:N)
    @OneToMany(
        mappedBy = "disease", // DiseaseSpecialty 엔티티에서 disease 필드를 기준으로 관계 설정
        cascade = [CascadeType.ALL], // 질병 삭제 시 관련 DiseaseSpecialty도 함께 삭제
        orphanRemoval = true // 관계가 끊기면 DB에서 삭제됨
    )
    val diseaseSpecialties: List<DiseaseSpecialty> = emptyList(), // 해당 질병과 연결된 진료과 목록

    // 질병과 의사와의 관계 (1:N)
    @OneToMany(
        mappedBy = "disease", // DiseaseDoctor 엔티티에서 disease 필드를 기준으로 관계 설정
        cascade = [CascadeType.ALL], // 질병 삭제 시 관련 DiseaseDoctor도 함께 삭제
        orphanRemoval = true // 관계가 끊기면 DB에서 삭제됨
    )
    val diseaseDoctors: List<DiseaseDoctor> = emptyList() // 해당 질병과 연결된 의사 목록
)
