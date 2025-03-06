package com.callrapport.model.hospital

import com.callrapport.model.user.UserFavoriteHospital
import jakarta.persistence.*
import com.fasterxml.jackson.annotation.JsonManagedReference

@Entity
@Table(name = "hospitals")
data class Hospital(
    @Id
    val id: String, // 병원 ID (기본키)

    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val name: String, // 병원 이름 

    @Column(nullable = true) // 선택적 입력 값 (NULL 허용)
    val phoneNumber: String? = null, // 전화번호

    @Column(nullable = true) // 선택적 입력 값 (NULL 허용)
    val homepage: String? = null, // 홈페이지 URL 

    @Column(nullable = true) // 선택적 입력 값 (NULL 허용)
    val address: String? = null, // 병원 주소

    @Column(nullable = true) // 선택적 입력 값 (NULL 허용)
    val operatingHours: String? = null, // 병원 운영시간

    @Column(nullable = true) // 선택적 입력 값 (NULL 허용)
    val url: String? = null, // 병원 정보 페이지 URL

    @OneToMany( // 1:N 관계
        mappedBy = "hospital", // HospitalSpecialty 엔티티에서 hospital 필드를 기준으로 관계 설정
        cascade = [CascadeType.ALL],  // Hospital 삭제 시 관련 HospitalSpecialty도 함께 삭제
        orphanRemoval = true // HospitalSpecialty가 Hospital에서 제거되면 DB에서도 삭제됨
    ) 
    val specialties: List<HospitalSpecialty> = mutableListOf(), // 진료과목

    @OneToMany( // 1:N 관계
        mappedBy = "hospital", // HospitalDoctor 엔티티에서 hospital 필드를 기준으로 관계 설정
        cascade = [CascadeType.ALL], // Hospital 삭제 시 관련 HospitalDoctor도 함께 삭제
        orphanRemoval = true // HospitalDoctor가 Hospital에서 제거되면 DB에서도 삭제됨
    )
    val doctors: List<HospitalDoctor> = mutableListOf(), // 병원 소속 의사

    @OneToOne( // 1:N 관계 
        mappedBy = "hospital", // HospitalAdditionalInfo 엔티티에서 hospital 필드를 기준으로 관계 설정
        cascade = [CascadeType.ALL], // Hospital 삭제 시 관련 HospitalAdditionalInfo도 함께 삭제
        orphanRemoval = true) // HospitalAdditionalInfo가 Hospital에서 제거되면 DB에서도 삭제됨
    @JsonManagedReference // 순환 참조 방지
    val additionalInfo: HospitalAdditionalInfo? = null, // 병원 부가 정보

    @OneToMany(
        mappedBy = "hospital", // UserFavoriteHospital 엔티티에서 hospital 필드를 기준으로 관계 설정
        cascade = [CascadeType.ALL], // Hospital 삭제 시 관련 UserFavoriteHospital도 함께 삭제
        orphanRemoval = true) // UserFavoriteHospital이 Hospital에서 제거되면 DB에서도 삭제됨
    val favoritedByUsers: MutableList<UserFavoriteHospital> = mutableListOf() // 병원을 즐겨찾기한 사용자 목록
)
