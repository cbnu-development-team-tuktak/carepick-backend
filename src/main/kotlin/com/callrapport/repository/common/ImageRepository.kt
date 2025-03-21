package com.callrapport.repository.common

// Model (엔티티) 관련 import 
import com.callrapport.model.common.Image // 이미지 엔티티

// Spring Data JPA 관련 import 
import org.springframework.stereotype.Repository // 해당 인터페이스가 데이터베이스 접근 레이어(Repository)임을 나타내는 어노테이션
import org.springframework.data.jpa.repository.JpaRepository // JPA에서 기본적인 CRUD (Create, Read, Update, Delete) 메서드를 제공하는 인터페이스

@Repository
interface ImageRepository : JpaRepository<Image, Long> {
    // 이미지 URL을 기준으로 존재 여부를 확인
    fun existsByUrl(
        url: String // 검사할 이미지 URL
    ): Boolean // 해당 URL의 이미지가 존재하는지 여부 반환

    // 이미지 URL로 이미지 정보 조회
    fun findByUrl(
        url: String // 검색할 이미지
    ): Image? // 해당 URL과 일치하는 Image 객체 또는 null
}
