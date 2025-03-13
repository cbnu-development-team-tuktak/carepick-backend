package com.callrapport.model.hospital

import com.callrapport.model.user.UserFavoriteHospital
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import org.locationtech.jts.geom.Point

@Entity
@Table(
    name = "hospitals",
    indexes = [
        Index(name = "idx_hospital_location", columnList = "location", unique = false)
    ]
)
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

    // ✅ 병원과 진료과의 관계 (1:N)
    @OneToMany(
        mappedBy = "hospital", // HospitalSpecialty 엔티티에서 hospital 필드를 기준으로 관계 설정
        cascade = [CascadeType.ALL],  // 병원 삭제 시 HospitalSpecialty도 함께 삭제
        orphanRemoval = true // 관계가 끊기면 DB에서 삭제됨
    ) 
    var specialties: MutableList<HospitalSpecialty> = mutableListOf(), // 진료과목 목록

    // ✅ 병원과 의사의 관계 (1:N)
    @OneToMany(
        mappedBy = "hospital", // HospitalDoctor 엔티티에서 hospital 필드를 기준으로 관계 설정
        cascade = [CascadeType.ALL], // 병원 삭제 시 HospitalDoctor도 함께 삭제
        orphanRemoval = true // 관계가 끊기면 DB에서 삭제됨
    )
    var doctors: MutableList<HospitalDoctor> = mutableListOf(), // 병원 소속 의사 목록

    // ✅ 병원과 추가 정보의 관계 (1:1)
    @OneToOne(
        mappedBy = "hospital", // HospitalAdditionalInfo 엔티티에서 hospital 필드를 기준으로 관계 설정
        cascade = [CascadeType.ALL], // 병원 삭제 시 HospitalAdditionalInfo도 함께 삭제
        orphanRemoval = true // 관계가 끊기면 DB에서 삭제됨
    )
    @JsonManagedReference // 순환 참조 방지
    var additionalInfo: HospitalAdditionalInfo? = null, // 병원 부가 정보

    // ✅ 병원을 즐겨찾기한 사용자와의 관계 (1:N)
    @OneToMany(
        mappedBy = "hospital", // UserFavoriteHospital 엔티티에서 hospital 필드를 기준으로 관계 설정
        cascade = [CascadeType.ALL], // 병원 삭제 시 UserFavoriteHospital도 함께 삭제
        orphanRemoval = true // 관계가 끊기면 DB에서 삭제됨
    )
    var favoritedByUsers: MutableList<UserFavoriteHospital> = mutableListOf(), // 병원을 즐겨찾기한 사용자 목록

    // ✅ 병원의 위치 정보 (공간 데이터)
    @Column(
        nullable = true, // 선택적 입력 값 (NULL 허용)
        columnDefinition = "POINT SRID 4326" // 공간 데이터(Point) 타입, GPS 좌표계(SRID 4326 - WGS 84) 사용
    )
    var location: Point? = null // 병원의 좌표 정보
)
