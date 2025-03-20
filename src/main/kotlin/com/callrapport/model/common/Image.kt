package com.callrapport.model.common

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "images",
    uniqueConstraints = [UniqueConstraint(columnNames = ["url"])] 
)
data class Image(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null, // 이미지 기본키

    @Column(
        nullable = false,
        unique = true
    )
    val url: String, // 이미지 url

    @Column(nullable = false)
    val alt: String, // 이미지 alt 텍스트
    
    @Column(nullable = false) // 필수 입력 값 (NULL 값 허용 안함)
    val createdAt: LocalDateTime = LocalDateTime.now(), // 즐겨찾기 추가 시간 (기본값: 현재 시간)

    @Column(nullable = false) // 필수 입력 값 (NULL 값 허용 안함)
    val updatedAt: LocalDateTime = LocalDateTime.now() // 즐겨찾기 추가 시간 (기본값: 현재 시간)
)