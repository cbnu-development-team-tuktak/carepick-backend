package com.callrapport.repository.hospital

// 엔티티 관련 import 
import com.callrapport.model.hospital.OperatingHours 

// Spring Data JPA 관련 import 
import org.springframework.data.jpa.repository.JpaRepository // JPA에서 기본적인 CRUD (Create, Read, Update, Delete) 메서드를 제공하는 인터페이스
import org.springframework.stereotype.Repository // 해당 인터페이스가 데이터 접근 레이어(Repository)임을 나타내는 어노테이션

@Repository
interface OperatingHoursRepository : JpaRepository<OperatingHours, String> {
}
