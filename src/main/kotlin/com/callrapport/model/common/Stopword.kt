package com.callrapport.model.common

// JPA 관련 import 
import jakarta.persistence.* // JPA 엔티티 매핑을 위한 어노테이션 포함

@Entity
@Table(name = "stopwords")
class Stopword(
    @Id // 기본 키
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 설정
    val id: Long = 0L,

    @Column(
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        unique = true // 동일한 단어 중복 저장 방지
    ) 
    val word: String, // 불용어 단어

    @Column(
        nullable = false // 필수 입력 값 (NULL 허용 안 함)
    ) 
    // 명사, 대명사, 수사, 동사, 형용사, 관형사, 부사, 조사, 감탄사, 보조용언
    val pos: String // 품사
)