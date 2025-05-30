package com.callrapport.repository.doctor

// Model (엔티티) 관련 import 
import com.callrapport.model.doctor.Doctor // Doctor: 의사 정보를 저장하는 엔티티 (ID, 이름, 프로필 이미지, 진료과 등)

// Spring Data JPA 관련 import
import org.springframework.stereotype.Repository // 해당 인터페이스가 데이터베이스 접근 레이어(Repository)임을 나타내는 어노테이션
import org.springframework.data.jpa.repository.JpaRepository // JPA에서 기본적인 CRUD (Create, Read, Update, Delete) 메서드를 제공하는 인터페이스
import org.springframework.data.jpa.repository.Query // JPA에서 사용자 정의 JPQL (쿼리 메서드)를 작성할 때 사용하는 어노테이션
import org.springframework.data.repository.query.Param // @Query에서 JPQL의 매개변수를 바인딩할 때 사용하는 어노테이션

import org.springframework.data.domain.Page // 페이지네이션을 지원하는 JPA의 기본 객체 (검색 결과를 페이지 단위로 관리)
import org.springframework.data.domain.Pageable // 페이지네이션 요청을 처리하는 JPA 객체 (클라이언트가 요청한 페이지 정보 포함)

// 공간 데이터 관련 import
import org.locationtech.jts.geom.Coordinate // 좌표를 나타내는 객체 (x: 경도, y: 위도)
import org.locationtech.jts.geom.GeometryFactory // Geometry 객체 생성을 위한 팩토리 클래스
import org.locationtech.jts.geom.Point // 포인트(위치)를 나타내는 공간 객체
import org.locationtech.jts.geom.PrecisionModel // 좌표의 정밀도 모델 설정 클래스

@Repository
interface DoctorRepository : JpaRepository<Doctor, String> {
    // 의사 이름을 기준으로 검색
    // Like %:keyword%: 부분 일치 검색을 수행하여 특정 키워드가 포함된 이름 검색 가능
    // Pageable을 사용하여 검색 결과를 페이지 단위로 반환
    @Query("SELECT d FROM Doctor d WHERE d.name LIKE %:keyword%")
    fun searchByName(
        @Param("keyword") keyword: String, // 검색할 이름 키워드
        pageable: Pageable // 페이지네이션 정보를 포함한 객체
    ): Page<Doctor> // 페이지 단위의 검색된 Doctor 목록

    // 모든 의사 정보를 페이지네이션으로 조회
    // 페이지네이션을 적용하기 위해 오버라이드
    override fun findAll(
        pageable: Pageable // 페이지네이션 정보를 포함한 객체
    ): Page<Doctor> // 페이지 단위의 모든 Doctor 목록

    @Query(
        """
        SELECT d FROM Doctor d
        LEFT JOIN d.careers c
        LEFT JOIN HospitalDoctor hd ON hd.doctor = d
        LEFT JOIN Hospital h ON hd.hospital = h
        WHERE
            (:keyword IS NULL OR d.name LIKE %:keyword%)
        GROUP BY d
        ORDER BY
            CASE
                WHEN :sortBy = 'education' THEN d.totalEducationLicenseScore
                ELSE NULL
            END DESC,
            CASE
                WHEN :sortBy = 'distance' THEN function('ST_Distance_Sphere', h.location, :location)
                ELSE NULL
            END ASC
        """
    )
    fun searchDoctorsByFilters(
        @Param("keyword") keyword: String?, // 의사 이름 키워드 (부분 일치)
        @Param("location") location: Point?, // 사용자 위치 (좌표 정보)
        @Param("sortBy") sortBy: String, // 정렬 기준: 학력순(education) 또는 가까운 병원 소속순(distance)
        pageable: Pageable // 페이지네이션 정보
    ): Page<Doctor> // 필터에 해당하는 의사 목록
}
