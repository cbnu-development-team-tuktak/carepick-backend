package com.callrapport.model.disease

// 엔티티 관련 import
import com.callrapport.model.common.Specialty // Specialty: 진료과 정보
import com.callrapport.model.disease.Disease // Disease: 정제된 질병 정보

// JPA 관련 import
import jakarta.persistence.* // JPA 어노테이션 포함

@Entity
@Table(name = "disease_specialty")
data class DiseaseSpecialty(
    @Id // 기본 키(Primary Key) 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 ID
    val id: Long = 0L, // 고유 ID

    @ManyToOne // 질병과 다대일(N:1) 관계
    @JoinColumn(
        name = "disease_id", // 외래 키 이름
        referencedColumnName = "id", // Disease 테이블의 ID 참조
        nullable = false // 필수 입력
    )
    val disease: Disease, // 연결된 질병

    @ManyToOne // 진료과(Specialty)와 다대일(N:1) 관계
    @JoinColumn(
        name = "specialty_id", // 외래 키 이름
        referencedColumnName = "id", // Specialty 테이블의 ID 참조
        nullable = false // 필수 입력
    )
    val specialty: Specialty // 관련된 진료과
)