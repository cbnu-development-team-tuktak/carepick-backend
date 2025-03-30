package com.callrapport.model.disease

// JPA 관련 import
import jakarta.persistence.* // JPA 엔티티 매핑 및 컬럼 설정, enum 처리 등을 위한 어노테이션 제공

// 날짜/시간 관련 import
import java.time.LocalDateTime // 생성일(createdAt), 수정일(updatedAt) 타입으로 사용

@Entity
@Table(name = "disease_raw") 
data class DiseaseRaw(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 자동 생성 (Auto Increment)
    val id: Long = 0, // 질병 데이터 고유 ID (Primary Key)

    @Column(
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        unique = true     // 중복 허용 안 함 (질병명은 유일해야 함)
    )
    val name: String, // 질병명 (예: 폐렴, 대상포진 등)

    @Column(
        name = "body_system", // 실제 DB 컬럼명을 'body_system'으로 설정
        nullable = false      // 필수 입력 값 (NULL 허용 안 함)
    )
    val bodySystem: String, // 신체계통 정보 (예: 호흡기, 피부, 순환기 등)

    @Column(
        nullable = false,           // 필수 입력 값 (NULL 허용 안 함)
        columnDefinition = "TEXT"   // 긴 텍스트 저장을 위한 TEXT 타입 지정
    )
    val symptoms: String, // 해당 질병의 주요 증상 설명

    @Column(
        nullable = false // 필수 입력 값 (NULL 허용 안 함)
    )
    val url: String, // 질병 상세 페이지의 원본 URL

    @Enumerated(EnumType.STRING) // Enum 값을 문자열(String)로 저장
    @Column(
        nullable = false // 필수 입력 값 (NULL 허용 안 함)
    )
    var status: DiseaseStatus = DiseaseStatus.PENDING, // 처리 상태 (PENDING: 대기, COMPLETED: 완료, FAILED: 실패)

    @Column(
        name = "created_at", // 실제 DB 컬럼명을 'created_at'으로 설정
        updatable = false    // 최초 생성 시에만 설정되고 수정 불가
    )
    val createdAt: LocalDateTime = LocalDateTime.now(), // 데이터 최초 저장 시각

    @Column(
        name = "updated_at" // 실제 DB 컬럼명을 'updated_at'으로 설정
    )
    var updatedAt: LocalDateTime = LocalDateTime.now() // 마지막 수정 시각
)

// CHATGPT가 증상 추출 및 진료과 매칭 작업을 수행했는지 나타내는 처리 상태
enum class DiseaseStatus {
    PENDING,   // 처리 대기 중 (ChatGPT 요약 및 매칭 작업 전)
    COMPLETED, // ChatGPT 요약 및 진료과 매칭 작업 성공
    FAILED     // 처리 중 오류 발생 (요약 실패 또는 매칭 실패 등)
}
