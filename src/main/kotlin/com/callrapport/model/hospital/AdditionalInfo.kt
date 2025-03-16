package com.callrapport.model.hospital

// JPA 관련 import
import jakarta.persistence.* // JPA 엔티티 매핑을 위한 어노테이션 포함

@Entity
@Table(name = "additional_info")
data class AdditionalInfo(
    @Id // 기본 키(Primary Key) 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID 값을 자동 증가(Auto Increment)하도록 설정
    val id: Long? = null, // 추가 정보 ID (자동 생성됨)

    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val open24Hours: Boolean = false, // 24시간 문의 가능 여부

    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val emergencyTreatment: Boolean = false, // 24시간 응급 환자 진료 가능 여부

    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val maleFemaleDoctorChoice: Boolean = false, // 남여 전문의 선택 가능 여부

    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val networkHospital: Boolean = false, // 네트워크 병원 여부

    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val freeCheckup: Boolean = false, // 무료 검진 제공 여부

    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val nearSubway: Boolean = false, // 역세권 위치 여부

    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val openAllYear: Boolean = false, // 연중무휴 진료 여부

    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val openOnSunday: Boolean = false, // 일요일 & 공휴일 진료 여부

    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val nightShift: Boolean = false, // 평일 야간 진료 여부

    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val collaborativeCare: Boolean = false, // 협진 시스템 지원 여부

    @Column(nullable = false) // 필수 입력 값 (NULL 허용 안 함)
    val noLunchBreak: Boolean = false // 점심시간 없이 진료 여부
)
