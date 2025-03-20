package com.callrapport.repository.common

// Model (엔티티) 관련 import 
import com.callrapport.model.common.Specialty

// Spring Data JPA 관련 import
import org.springframework.stereotype.Repository // 해당 인터페이스가 데이터베이스 접근 레이어(Repository)임을 나타내는 어노테이션
import org.springframework.data.jpa.repository.JpaRepository // JPA에서 기본적인 CRUD (Create, Read, Update, Delete) 메서드를 제공하는 인터페이스

@Repository
interface SpecialtyRepository : JpaRepository<Specialty, String> {
    // 진료과 이름을 기준으로 존재 여부를 확인
    fun existsByName(
        name: String // 검색할 진료과 이름
    ): Boolean // 진료과 존재 여부

    // 진료과 이름을 기준으로 검색
    fun findByName(
        name: String // 검색할 진료과 이름
    ): Specialty? // 해당 이름과 일치하는 Specialty 객체 
}

