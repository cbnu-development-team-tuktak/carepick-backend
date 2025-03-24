package com.callrapport.repository.common

// Spring Data JPA 관련 import
import org.springframework.stereotype.Repository // 해당 인터페이스가 데이터베이스 접근 레이어(Repository)임을 나타내는 어노테이션
import org.springframework.data.jpa.repository.JpaRepository // JPA에서 기본적인 CRUD (Create, Read, Update, Delete) 메서드를 제공하는 인터페이스

// 엔티티 관련 import
import com.callrapport.model.common.Stopword

@Repository
interface StopwordRepository : JpaRepository<Stopword, Long> {
    // 특정 단어가 이미 존재하는지 확인
    fun existsByWord(
        word: String // 검색할 불용어 단어
    ): Boolean // 해당 단어가 존재하면 true, 존재하지 않으면 false

    // 특정 품사(pos)를 가진 불용어 전체 조회
    fun findAllByPos(
        pos: String // 조회할 품사
    ): List<Stopword> // 해당 품사의 불용어 목록

    // 단어로 불용어 조회
    fun findByWord(
        word: String // 검색할 불용어 단어
    ): Stopword? // 해당 단어에 일치하는 불용어가 있으면 반환, 없으면 null
}