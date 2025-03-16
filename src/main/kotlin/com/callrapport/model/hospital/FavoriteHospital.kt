package com.callrapport.model.user

// 엔티티 관련 import
import com.callrapport.model.hospital.Hospital // Hospital: 병원 정보를 저장하는 엔티티
import com.callrapport.model.user.User // User: 사용자 정보를 저장하는 엔티티

// JPA 관련 import
import jakarta.persistence.* // JPA 매핑을 위한 어노테이션 포함

// Java 시간 관련 import
import java.time.LocalDateTime // LocalDateTime: 날짜 및 시간 저장을 위한 클래스

@Entity
@Table(name = "user_favorite_hospitals")
data class UserFavortieHospital(
    @Id // 기본 키(Primary Key) 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID 값을 자동 증가(Auto Increment)하도록 설정
    val id: Long? = null, // 즐겨찾기 ID (자동 생성됨)

    @ManyToOne // 사용자(User)와 다대일(N:1) 관계
    @JoinColumn(
        name = "user_id", // 매핑할 외래 키(FK) 이름
        referencedColumnName = "id", // User 엔티티의 "id" 컬럼을 참조
        nullable = false // 필수 입력 값 (NULL 허용 안 함)
    )
    val user: User, // 즐겨찾기한 사용자

    @ManyToOne // 병원(Hospital)과 다대일(N:1) 관계
    @JoinColumn(
        name = "hospital_id", // 매핑할 외래 키(FK) dlfma
        referencedColumnName = "id", // Hospital 엔티티의 "id" 컬럼을 참조
        nullable = false // 필수 입력 값 (NULL 허용 안 함)
    )
    val hospital: Hospital, // 즐겨찾기한 병원

    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val createdAT: LocalDateTime = LocalDateTime.now() // 즐겨찾기 추가 시간
)