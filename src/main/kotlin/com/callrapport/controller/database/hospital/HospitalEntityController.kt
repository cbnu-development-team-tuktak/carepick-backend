package com.callrapport.controller

// DTO import 
import com.callrapport.dto.HospitalDetailsResponse // 병원 정보 응답용 DTO

// 엔티티 import 
import com.callrapport.model.hospital.Hospital // 병원 엔티티

// 서비스 import 
import com.callrapport.service.HospitalService // 병원 관련 비즈니스 로직 처리 서비스 

// Spring Data JPA 관련 import
import org.springframework.data.domain.Page // 페이징 결과를 담는 객체
import org.springframework.data.domain.Pageable // 페이징 요청 정보를 담는 객체

// Spring Web 관련 import 
import org.springframework.http.ResponseEntity // HTTP 응답 객체
import org.springframework.web.bind.annotation.* // REST 컨트롤러 관련 어노테이션들

// 공간 데이터 관련 import
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.PrecisionModel

import java.time.LocalTime

@RestController
@RequestMapping("/api/hospitals")
class HospitalEntityController(
    private val hospitalService: HospitalService // 병원 데이터를 처리하는 서비스 의존성 주입 
) {
    // 병원 개수 조회
    // 예: http://localhost:8080/api/hospitals/count
    @GetMapping("/count")
    fun getSymptomsCount(): ResponseEntity<Map<String, Long>> {
        val count = hospitalService.countAllHospitals()  // 전체 증상 개수 조회
        return ResponseEntity.ok(mapOf("count" to count))
    }

    // 병원명으로 검색
    // 예: http://localhost:8080/api/hospitals/search?keyword=베이드의원&page=0&size=10
    @GetMapping("/search")
    fun searchHospitals(
        @RequestParam keyword: String, // 검색 키워드 (병원 이름)
        pageable: Pageable // 페이징 정보
    ): Page<HospitalDetailsResponse> {
        // 병원명 기준 검색 후 DTO로 변환
        val hospitalPage = hospitalService.searchHospitalsByName(keyword, pageable)
        return hospitalPage.map { HospitalDetailsResponse.from(it) }
    }

    // 주소로 병원 검색
    // 예: http://localhost:8080/api/hospitals/search/address?keyword=강남&page=0&size=10
    @GetMapping("/search/address")
    fun searchHospitalsByAddress(
        @RequestParam keyword: String, // 검색 키워드 (주소 일부)
        pageable: Pageable // 페이징 정보
    ): Page<HospitalDetailsResponse> {
        // 주소 기준 검색 후 DTO로 변환
        val hospitalPage = hospitalService.searchHospitalsByAddress(keyword, pageable)
        return hospitalPage.map { HospitalDetailsResponse.from(it) }
    }

    // 모든 병원 목록 조회 (DTO 반환)
    // 예: http://localhost:8080/api/hospitals?page=0&size=10
    @GetMapping
    fun getAllHospitals(
        pageable: Pageable // 페이징 정보
    ): Page<HospitalDetailsResponse> {
        // 전체 병원 조회 후 DTO로 변환
        val hospitalPage = hospitalService.getAllHospitals(pageable)
        return hospitalPage.map { HospitalDetailsResponse.from(it) }
    }

    // 병원 ID로 단일 병원 조회
    // 예: http://localhost:8080/api/hospitals/H0000238449
    @GetMapping("/{id}")
    fun getHospitalById(
        @PathVariable id: String
    ): HospitalDetailsResponse {
        // 서비스 계층을 통해 병원 엔티티 조회
        val hospital = hospitalService.getHospitalById(id)
        // 조회된 병원 엔티티를 DTO로 변환하여 응답
        return HospitalDetailsResponse.from(hospital)
    }

    // 위치를 기준으로 병원 검색 (근처 병원 순으로 정렬)
    // 예: http://localhost:8080/api/hospitals/location?lat=36.6242237&lng=127.4614843&page=0&size=10
    @GetMapping("/location")
    fun searchHospitalsByLocation(
        @RequestParam lat: Double, 
        @RequestParam lng: Double, 
        pageable: Pageable
    ): Page<HospitalDetailsResponse> {
        val coordinate = Coordinate(lng, lat) // 경도, 위도 순서
        val geometryFactory = GeometryFactory(PrecisionModel(), 4326)
        val location = geometryFactory.createPoint(coordinate)

        val hospitalPage = hospitalService.getHospitalsByLocation(location, pageable)
        return hospitalPage.map { HospitalDetailsResponse.from(it) }
    }

    // 필터 및 정렬 기준을 반영한 병원 검색
    // 예: http://localhost:8080/api/hospitals/filter?keyword=서울&lat=36.6242237&lng=127.4614843&distance=3&specialties=성형외과&sortBy=distance&selectedDays=월&startTime=09:00&endTime=18:00&page=0&size=10
    @GetMapping("/filter")
    fun searchHospitalsByFilters(
        @RequestParam(required = false) keyword: String?, // 병원 이름 키워드 (부분 일치)
        @RequestParam(required = false) lat: Double?, // 위도
        @RequestParam(required = false) lng: Double?, // 경도
        @RequestParam(required = false) distance: Double?, // 거리 (km)
        @RequestParam(required = false) specialties: List<String>?, // 진료과
        @RequestParam(required = false) selectedDays: List<String>?, // 요일 필터 (예: 월, 화)
        @RequestParam(required = false) startTime: LocalTime?, // 시작 시간 필터
        @RequestParam(required = false) endTime: LocalTime?, // 종료 시간 필터
        @RequestParam(defaultValue = "distance") sortBy: String, // 정렬 기준
        pageable: Pageable
    ): Page<HospitalDetailsResponse> {
        // 좌표 객체 생성 (위도와 경도가 모두 존재할 경우만 생성)
        val location = if (lat != null && lng != null) {
            val coordinate = Coordinate(lng, lat) // 위도(lat), 경도(lng)를 좌표 객체로 반환
            val geometryFactory = GeometryFactory(PrecisionModel(), 4326) // WGS84 좌표계 (SRID: 4326) 사용
            geometryFactory.createPoint(coordinate) // 좌표(Point) 객체 생성
        } else null // 위도 또는 경도 값이 없으면 null 처리

        // 정렬 기준 검증 및 기본값 설정
        val validSortBy = when (sortBy.lowercase()) {
            "distance", "name" -> sortBy.lowercase() // 유효한 정렬 기준이면 그대로 사용
            else -> "distance" // 잘못된 값이면 기본값 "distance" 사용
        }

        // 병원 검색 서비스 호출
        val hospitalPage = hospitalService.getHospitalsByFilters(
            keyword = keyword, // 병원 이름 키워드 (부분 일치)
            location = location, // 위치 정보 (Point 좌표)
            maxDistanceInKm = distance, // 최대 거리 제한 (km 단위)
            specialties = specialties, // 진료과 필터
            selectedDays = selectedDays, // 요일 필터 
            startTime = startTime, // 희망 시작 시간
            endTime = endTime, // 희망 종료 시간
            sortBy = validSortBy, // 정렬 기준 (distance 또는 name)
            pageable = pageable // 페이지네이션 정보
        )

        // 검색할 병원 목록을 HospitalDetailsResponse 형태로 매핑하여 변환
        return hospitalPage.map { HospitalDetailsResponse.from(it) }
    }
}
