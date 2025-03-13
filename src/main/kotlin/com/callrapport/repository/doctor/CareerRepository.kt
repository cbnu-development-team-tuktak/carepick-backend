package com.callrapport.repository.doctor

import com.callrapport.model.doctor.Career
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CareerRepository : JpaRepository<Career, Long> {
    fun findByName(name: String): Career? // ✅ 경력 내용을 이름으로 조회
}
