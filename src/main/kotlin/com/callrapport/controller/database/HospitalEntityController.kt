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
    // 예: http://localhost:8080/api/hospitals/filter?lat=36.6242237&lng=127.4614843&distance=3&specialties=소아청소년과&specialties=내과&sortBy=distance&page=0&size=10
    @GetMapping("/filter")
    fun searchHospitalsByFilters(
        @RequestParam(required = false) lat: Double?, // 위도 (nullable)
        @RequestParam(required = false) lng: Double?, // 경도 (nullable)
        @RequestParam(required = false) distance: Double?, // 거리 (km 단위)
        @RequestParam(required = false) specialties: List<String>?, // 진료과 리스트
        @RequestParam(defaultValue = "distance") sortBy: String, // 정렬 기준 ("distance" or "name")
        pageable: Pageable // 페이지네이션 정보 (page, size 등)
    ): Page<HospitalDetailsResponse> {
        // 위도와 경도가 모두 존재할 경우에만 위치(Point) 객체 생성 (좌표계: WGS 84)
        val location = if (lat != null && lng != null) {
            val coordinate = Coordinate(lng, lat) // 경도(lng), 위도(lat) 순서로 입력
            val geometryFactory = GeometryFactory(PrecisionModel(), 4326) // 좌표 정밀도 설정 + SRIG 4326
            geometryFactory.createPoint(coordinate) // Point 객체 생성
        } else {
            null // 둘 중 하나라도 null이면 거리 필터 미적용
        }

        // 정렬 기준 유효성 체크
        val validSortBy = when (sortBy.lowercase()) {
            "distance", "name" -> sortBy.lowercase() // distance 또는 name일 경우 그대로 사용
            else -> "distance" // 잘못된 값이면 기본값 사용
        }

        // 병원 검색 서비스 호출 
        val hospitalPage = hospitalService.getHospitalsByFilters(
            location = location, // 기준 위치
            maxDistanceInKm = distance, // 최대 검색 거리 (km)
            specialties = specialties, // 진료과 리스트
            sortBy = validSortBy, // 정렬 기준
            pageable = pageable // 페이징 정보
        )

        // 엔티티 → 응답 DTO로 매핑하여 반환
        return hospitalPage.map { HospitalDetailsResponse.from(it) }
    }
}
