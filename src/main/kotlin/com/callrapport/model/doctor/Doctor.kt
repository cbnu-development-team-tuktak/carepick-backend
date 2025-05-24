package com.callrapport.model.doctor

// JSON 직렬화 관련 import
import com.fasterxml.jackson.annotation.JsonManagedReference // 순환 참조 방지를 위해 사용

// 엔티티 관련 import
import com.callrapport.model.common.Specialty // Specialty: 의사의 진료과 정보를 저장하는 엔티티

// JPA 관련 import 
import jakarta.persistence.*

@Entity
@Table(name = "doctors")
data class Doctor(
    @Id // 기본 키(Primary) 설정
    val id: String, // 의사 ID (기본 키, 문자열 타입)

    @Column(
        nullable = false // 필수 입력 값 (NULL 허용 안 함)
    ) 
    val name: String, // 의사 이름

    @Column(
        nullable = true // 선택적 입력 값 (NULL 허용)
    ) 
    val profileImage: String? = null, // 프로필 이미지 URL  

    // 의사와 경력(1:N) 관계
    @OneToMany(
        mappedBy = "doctor", // DoctorCareer 엔티티의 doctor 필드와 매핑
        cascade = [CascadeType.ALL], // Doctor 삭제 시 관련 데이터도 함께 삭제됨 
        orphanRemoval = true // 고아 객체 자동 삭제
    )
    val careers: MutableList<DoctorCareer> = mutableListOf(), // 의사의 경력 정보
    
    // 의사와 학력 및 자격면허(1:N) 관계
    @OneToMany(
        mappedBy = "doctor", // DoctorEducationLicense 엔티티의 doctor 필드와 매핑
        cascade = [CascadeType.ALL], // Doctor 삭제 시 관련 데이터도 함께 삭제됨
        orphanRemoval = true // 고아 객체 자동 삭제
    )
    val educationLicenses: MutableList<DoctorEducationLicense> = mutableListOf(), // 의사의 학력 및 자격면허 정보 

    @Column(
        nullable = false // 필수 입력 값 (NULL 허용 안 함)
    ) 
    var totalEducationLicenseScore: Double = 0.0, // 의사의 학력/자격면허 점수 총합

    // 의사와 진료과(N:M) 관계를 관리하는 DoctorSpecialty와 1:N 관계
    @OneToMany(
        mappedBy = "doctor", // DoctorSpecialty 엔티티의 doctor 필드와 매핑
        cascade = [CascadeType.ALL], // Doctor 삭제 시 관련 데이터도 함께 삭제됨
        orphanRemoval = true // 고아 객체 자동 삭제
    )
    @JsonManagedReference // 순환 참조 방지
    var specialties: MutableList<DoctorSpecialty> = mutableListOf() // 의사의 진료과 목록
)
