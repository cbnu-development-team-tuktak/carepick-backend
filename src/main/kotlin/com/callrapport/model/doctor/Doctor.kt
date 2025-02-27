package com.callrapport.model.doctor

// Model (엔티티) 관련 import
import com.callrapport.model.common.Specialty // Specialty 엔티티: 의사의 진료과 정보를 저장하는 엔티티

// JPA 관련 import 
import jakarta.persistence.*

// JSON 직렬화 관련 import
import com.fasterxml.jackson.annotation.JsonIgnore // 특정 필드를 JSON 변환 시 제외하는 어노테이션 (순환 참조 방지)

@Entity
@Table(name = "doctors")
data class Doctor(
    @Id // 기본 키
    val id: String, // 의사 ID (기본키)

    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val name: String, // 의사 이름

    @Column(nullable = true) // 선택적 입력 값 (NULL 허용)
    val profileImage: String?, // 프로필 이미지 URL  

    @OneToMany( // 1:N 관계
        mappedBy = "doctor", // DoctorEducationLicense 엔티티에서 doctor 필드를 기준으로 관계 설정
        cascade = [CascadeType.ALL], // Doctor 삭제 시 관련 DoctorEducationLicense도 함께 삭제
        orphanRemoval = true, // DctorEducationLicense가 Doctor에서 제거되면 DB에서도 삭제됨
        fetch = FetchType.EAGER // Doctor 조회 시 educationLicenses도 즉시 가져옴
    )  
    val educationLicenses: List<DoctorEducationLicense> = mutableListOf(), // 의사의 학력 및 자격면허 정보 

    @Column(nullable = true) // 선택적 입력 값 (NULL 허용)
    val hospitalId: String?, // 병원 ID

    @ManyToOne // N:1 관계
    @JoinColumn(
        name = "specialty_id", // specialty_id를 외래키(FK)로 사용
        nullable = true // 선택적 입력 값 (NULL 허용)
    ) 
    val specialty: Specialty? // Specialty (진료과) 엔티티
)
