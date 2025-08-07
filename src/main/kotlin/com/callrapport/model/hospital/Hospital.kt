package com.callrapport.model.hospital

// 엔티티 관련 import
import com.callrapport.model.user.UserFavoriteHospital // UserFavoriteHospital: 사용자의 즐겨찾기 병원 정보를 저장하는 엔티티

// JSON 직렬화 관련 import
import com.fasterxml.jackson.annotation.JsonManagedReference // 순환 참조 방지를 위한 JSON 어노테이션

// JPA 관련 import
import jakarta.persistence.* // JPA 매핑을 위한 어노테이션 포함

// 공간 데이터(geo) 관련 import (병원 위치 좌표 관리)
import org.locationtech.jts.geom.Coordinate // 좌표 데이터 타입
import org.locationtech.jts.geom.GeometryFactory // 공간 데이터 객체 생성
import org.locationtech.jts.geom.Point // 병원 위치를 저장하는 Point 타입
import org.locationtech.jts.geom.PrecisionModel // 좌표 정밀도 설정

@Entity
@Table(
    name = "hospitals",
    indexes = [
        Index(name = "idx_hospital_location", columnList = "location", unique = false) // 위치 정보 인덱스 설정
    ]
)
data class Hospital(
    @Id // 기본 키(Primary Key) 설정
    val id: String, // 병원 ID (기본 키, 문자열 타입)

    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val name: String, // 병원 이름 

    @Column(nullable = true) // 선택적 입력 값 (NULL 허용)
    val phoneNumber: String? = null, // 전화번호

    @Column(nullable = true) // 선택적 입력 값 (NULL 허용)
    val homepage: String? = null, // 홈페이지 URL 

    @Column(nullable = true) // 선택적 입력 값 (NULL 허용)
    val address: String? = null, // 병원 주소

    @Column(nullable = true) // 선택적 입력 값 (NULL 허용)
    val url: String? = null, // 병원 정보 페이지 URL

    // 병원과 운영 시간 연결 테이블과의 관계 (1:N)
    @OneToMany(
        mappedBy = "hospital", // HospitalOperatingHours에서 hospital 필드를 기준으로 관계 설정
        cascade = [CascadeType.ALL], // 병원 삭제 시 연결된 운영시간도 삭제
        orphanRemoval = true // 관계가 끊기면 DB에서 삭제됨
    )
    var operatingHours: MutableList<HospitalOperatingHours> = mutableListOf(), // 병원과 연결된 운영 시간 목록

    // 병원과 진료과의 관계 (1:N)
    @OneToMany(
        mappedBy = "hospital", // HospitalSpecialty 엔티티에서 hospital 필드를 기준으로 관계 설정
        cascade = [CascadeType.ALL],  // 병원 삭제 시 HospitalSpecialty도 함께 삭제
        orphanRemoval = true // 관계가 끊기면 DB에서 삭제됨
    ) 
    var specialties: MutableList<HospitalSpecialty> = mutableListOf(), // 병원이 제공하는 진료과 목록

    // 병원과 의사의 관계 (1:N)
    @OneToMany(
        mappedBy = "hospital", // HospitalDoctor 엔티티에서 hospital 필드를 기준으로 관계 설정
        cascade = [CascadeType.ALL], // 병원 삭제 시 HospitalDoctor도 함께 삭제
        orphanRemoval = true // 관계가 끊기면 DB에서 삭제됨
    )
    var doctors: MutableList<HospitalDoctor> = mutableListOf(), // 병원 소속 의사 목록

    // 병원과 이미지의 관계 (1:N)
    @OneToMany(
        mappedBy = "hospital", // HospitalImage 엔티티에서 hospital 필드를 기준으로 관계 설정
        cascade = [CascadeType.ALL], // 병원 삭제 시 HospitalImage도 함께 삭제
        orphanRemoval = true // 관계가 끊기면 DB에서 삭제됨
    )
    var images: MutableList<HospitalImage> = mutableListOf(), // 병원 관련 이미지 목록
    
    // 병원과 추가 정보의 관계 (1:1)
    @OneToOne(
        mappedBy = "hospital", // HospitalAdditionalInfo 엔티티에서 hospital 필드를 기준으로 관계 설정
        cascade = [CascadeType.ALL], // 병원 삭제 시 HospitalAdditionalInfo도 함께 삭제
        orphanRemoval = true // 관계가 끊기면 DB에서 삭제됨
    )
    @JsonManagedReference // 순환 참조 방지
    var additionalInfo: HospitalAdditionalInfo? = null, // 병원 부가 정보 (예: 24시간 진료 여부 등)

    // 병원의 위치 정보 (공간 데이터)
    @Column(
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        columnDefinition = "POINT SRID 4326" // 공간 데이터(Point) 타입, GPS 좌표계(SRID 4326 - WGS 84) 사용
    )
    var location: Point = GeometryFactory(PrecisionModel(), 4326) // 병원의 좌표 정보
        .createPoint(Coordinate(127.0, 37.0)) // 기본값: 서울 중심 좌표 등으로 설정
)