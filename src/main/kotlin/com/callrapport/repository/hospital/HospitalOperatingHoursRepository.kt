package com.callrapport.repository.hospital

// 엔티티 관련 import 
import com.callrapport.model.hospital.HospitalOperatingHours 

// Spring Data JPA 관련 import 
import org.springframework.data.jpa.repository.JpaRepository // JPA에서 기본적인 CRUD (Create, Read, Update, Delete) 메서드를 제공하는 인터페이스
import org.springframework.stereotype.Repository // 해당 인터페이스가 데이터 접근 레이어(Repository)임을 나타내는 어노테이션

// JPQL을 활용한 사용자 정의 쿼리 메서드 관련
import org.springframework.data.jpa.repository.Query // JPA에서 사용자 정의 JPQL (쿼리 메서드)를 작성할 때 사용하는 어노테이션
import org.springframework.data.repository.query.Param // @Query에서 JPQL의 매개변수를 바인딩할 때 사용하는 어노테이션

@Repository
interface HospitalOperatingHoursRepository : JpaRepository<HospitalOperatingHours , String> {
    fun existsByHospitalIdAndOperatingHours_Day(hospitalId: String, day: String): Boolean
    fun findByHospitalIdAndOperatingHours_Day(hospitalId: String, day: String): HospitalOperatingHours?

    @Query("SELECT h FROM HospitalOperatingHours h WHERE h.hospital.id = :hospitalId AND h.operatingHours.day = :day")
    fun findByHospitalAndDay(
        @Param("hospitalId") hospitalId: String,
        @Param("day") day: String
    ): HospitalOperatingHours?
}
