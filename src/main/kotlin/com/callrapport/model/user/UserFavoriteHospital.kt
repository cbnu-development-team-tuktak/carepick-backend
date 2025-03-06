package com.callrapport.model.user

// Model (엔티티) 관련 import 
import com.callrapport.model.hospital.Hospital // Hospital 엔티티: 병원 정보를 저장하는 엔티티

// JPA 관련 import
import jakarta.persistence.*

// 시간 관련 import
import java.time.LocalDateTime // 즐겨찾기한 시간 저장을 위한 LocalDateTime

@Entity
@Table(name = "user_favortie_hospitals")
data class UserFavoriteHospital(
    @Id // 기본 키
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 값
    val id: Long? = null,

    @ManyToOne // N:1 관계
    @JoinColumn(
        name = "user_id", // user_id 를 외래 키(FK)로 사용
        referencedColumnName = "id", // User 엔티티의 id를 참조
        nullable = false // 필수 입력 값 (NULL 허용 안함)
    )
    val user: User, // 즐겨찾기한 사용자 정보

    @ManyToOne // N:1 관계
    @JoinColumn(
        name = "hospital_id", // hospital_id를 외래 키(FK)로 사용
        referencedColumnName = "id", // Hospital 엔티티의 id를 차조
        nullable = false // 필수 입력 값
    )
    val hospital: Hospital, // 즐겨찾기된 병원 정보

    @Column(nullable = false) // 필수 입력 값 (NULL 값 허용 안함)
    val createdAt: LocalDateTime = LocalDateTime.now() // 즐겨찾기 추가 시간
)