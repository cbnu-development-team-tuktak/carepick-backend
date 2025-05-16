package com.callrapport.model.user

// 엔티티 관련 import
import com.callrapport.model.hospital.Hospital // Hospital: 병원 정보를 저장하는 엔티티

// JPA 관련 import
import jakarta.persistence.* // JPA 매핑을 위한 어노테이션 포함

// Java 시간 관련 import
import java.time.LocalDateTime // 즐겨찾기한 시간 저장을 위한 LocalDateTime 클래스

@Entity
@Table(name = "user_favorite_hospitals")
data class UserFavoriteHospital(
    @Id // 기본 키(Primary Key) 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID 값을 자동 증가(Auto Increment)하도록 설정
    val id: Long? = null, // 즐겨찾기 ID (자동 생성됨)

    // 사용자(User)와 다대일(N:1) 관계
    @ManyToOne // 여러 개의 즐겨찾기 병원 데이터가 하나의 사용자와 연결될 수 있음
    @JoinColumn(
        name = "user_id", // 매핑할 외래 키(FK) 이름
        referencedColumnName = "id", // User 엔티티의 id 컬럼을 참조
        nullable = false // 필수 입력 값 (NULL 허용 안함)
    )
    val user: User, // 즐겨찾기한 사용자 정보

    // 병원(Hospital)과 다대일(N:1) 관계
    @ManyToOne // 여러 개의 즐겨찾기 병원 데이터가 하나의 병원과 연결될 수 있음
    @JoinColumn(
        name = "hospital_id", // 매핑할 외래 키(FK) 이름
        referencedColumnName = "id", // Hospital 엔티티의 id 컬럼을 참조
        nullable = false // 필수 입력 값
    )
    val hospital: Hospital, // 즐겨찾기된 병원 정보

    @Column(nullable = false) // 필수 입력 값 (NULL 값 허용 안함)
    val createdAt: LocalDateTime = LocalDateTime.now() // 즐겨찾기 추가 시간 (기본값: 현재 시간)
)