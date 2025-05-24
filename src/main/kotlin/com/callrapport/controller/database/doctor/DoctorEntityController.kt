package com.callrapport.controller.database.doctor

// DTO import 
import com.callrapport.dto.DoctorDetailsResponse // 의사 정보 응답용 DTO

// 서비스 import 
import com.callrapport.service.DoctorService // 의사 비즈니스 로직 서비스 

// Spring Data JPA 관련 import 
import org.springframework.data.domain.Page // 페이징된 응답을 위한 객체
import org.springframework.data.domain.PageImpl // 수동으로 Page 객체를 구성할 때 사용
import org.springframework.data.domain.Pageable // 페이징 정보(페이지 번호, 크기 등)를 담는 인터페이스

// Spring Web 관련 import 
import org.springframework.web.bind.annotation.* // REST 컨트롤러, 매핑, 요청 파라미터 어노테이션 등
import org.springframework.http.ResponseEntity // HTTP 응답 데이터를 표현하는 객체

// 공간 데이터 관련 import
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.PrecisionModel

@RestController
@RequestMapping("/api/doctors")
class DoctorEntityController(
    private val doctorService: DoctorService // 의사 데이터를 처리하는 서비스 
) {
    // 의사 개수 조회
    // 예: http://localhost:8080/api/doctors/count
    @GetMapping("/count")
    fun getDoctorsCount(): ResponseEntity<Map<String, Long>> {
        val count = doctorService.countAllDoctors()  // 전체 증상 개수 조회
        return ResponseEntity.ok(mapOf("count" to count))
    }
    // 의사 이름으로 검색
    // 예: http://localhost:8080/api/doctors/search?keyword=남호석&page=0&size=10
    @GetMapping("/search")
    fun searchDoctors(
        @RequestParam keyword: String, // 검색 키워드 (의사 이름)
        pageable: Pageable // 페이지 번호, 사이즈, 정렬 정보 등
    ): Page<DoctorDetailsResponse> {
        val doctorPage = doctorService.searchDoctorsByName(keyword, pageable) // 의사 엔티티 페이지 
        val dtoList = doctorPage.content.map { doctor ->
            val hospitalDoctor = doctorService.getFirstHospitalDoctorByDoctorId(doctor.id) // 소속 병원 조회
            DoctorDetailsResponse.from(doctor, hospitalDoctor) // 엔티티 → DTO 변환
        }
        return PageImpl(dtoList, pageable, doctorPage.totalElements) // PageImpl로 DTO 리스트 구성
    }

    // 전체 의사 목록 조회
    // 예: http://localhost:8080/api/doctors?page=0&size=10
    @GetMapping
    fun getAllDoctors(pageable: Pageable): Page<DoctorDetailsResponse> {
        val doctorPage = doctorService.getAllDoctors(pageable) // 모든 의사 엔티티 페이지 조회 
        val dtoList = doctorPage.content.map { doctor ->
            val hospitalDoctor = doctorService.getFirstHospitalDoctorByDoctorId(doctor.id) // 소속 병원 조회
            DoctorDetailsResponse.from(doctor, hospitalDoctor) // 엔티티 → DTO 변환
        }
        return PageImpl(dtoList, pageable, doctorPage.totalElements) // PageImpl로 DTO 리스트 구성
    }

    // 의사 ID로 단일 의사 정보 조회
    // 예: http://localhost:8080/api/doctors/U0000206325
    @GetMapping("/{id}")
    fun getDoctorById(
        @PathVariable id: String
    ): DoctorDetailsResponse {
        val doctor = doctorService.getDoctorById(id)
            ?: throw NoSuchElementException("Doctor with ID '$id' was not found.")
        
        val hospitalDoctor = doctorService.getFirstHospitalDoctorByDoctorId(id) // 소속 병원 조회

        return DoctorDetailsResponse.from(doctor, hospitalDoctor)
    }

    // 학력순 정렬 (위치 정보 포함)
    // 예: http://localhost:8080/api/doctors/sort/education?lat=37.5&lng=127.1&page=0&size=10 (좌표 적용)
    // 예: http://localhost:8080/api/doctors/sort/education?page=0&size=10 (좌표 미적용)
    @GetMapping("/sort/education")
    fun getDoctorsSortedByEducation(
        @RequestParam(required = false) lat: Double?,
        @RequestParam(required = false) lng: Double?,
        pageable: Pageable
    ): Page<DoctorDetailsResponse> {
        // 좌표가 둘 다 있으면 Point 생성
        val location = if (lat != null && lng != null) {
            val coordinate = Coordinate(lng, lat)
            val geometryFactory = GeometryFactory(PrecisionModel(), 4326)
            geometryFactory.createPoint(coordinate)
        } else null

        val doctorPage = doctorService.getDoctorsByFilters(
            keyword = null,
            location = location,
            sortBy = "education",
            pageable = pageable
        )

        val dtoList = doctorPage.content.map { doctor ->
            val hospitalDoctor = doctorService.getFirstHospitalDoctorByDoctorId(doctor.id)
            DoctorDetailsResponse.from(doctor, hospitalDoctor)
        }

        return PageImpl(dtoList, pageable, doctorPage.totalElements)
    }

    // 가까운 병원 소속순 정렬
    // 예: http://localhost:8080/api/doctors/sort/distance?lat=37.5&lng=127.1&page=0&size=10
    @GetMapping("/sort/distance")
    fun getDoctorsSortedByDistance(
        @RequestParam(required = true) lat: Double,
        @RequestParam(required = true) lng: Double,
        pageable: Pageable
    ): Page<DoctorDetailsResponse> {
        // 좌표 객체 생성
        val coordinate = Coordinate(lng, lat)
        val geometryFactory = GeometryFactory(PrecisionModel(), 4326)
        val location = geometryFactory.createPoint(coordinate)

        // 정렬 기준은 distance
        val doctorPage = doctorService.getDoctorsByFilters(
            keyword = null,
            location = location,
            sortBy = "distance",
            pageable = pageable
        )

        val dtoList = doctorPage.content.map { doctor ->
            val hospitalDoctor = doctorService.getFirstHospitalDoctorByDoctorId(doctor.id)
            DoctorDetailsResponse.from(doctor, hospitalDoctor)
        }

        return PageImpl(dtoList, pageable, doctorPage.totalElements)
    }

    // 경력순 정렬 (미구현)
    // 예: http://localhost:8080/api/doctors/sort/career?page=0&size=10
    @GetMapping("/sort/career")
    fun getDoctorsSortedByCareer(
        @RequestParam(required = false) lat: Double?,
        @RequestParam(required = false) lng: Double?,
        pageable: Pageable
    ): ResponseEntity<Map<String, Any>> {
        /*
        // 추후 경력순 정렬 로직이 구현되면 아래 로직 활성화 예정
        val location = if (lat != null && lng != null) {
            val coordinate = Coordinate(lng, lat)
            val geometryFactory = GeometryFactory(PrecisionModel(), 4326)
            geometryFactory.createPoint(coordinate)
        } else null

        val doctorPage = doctorService.getDoctorsByFilters(
            keyword = null,
            location = location,
            sortBy = "career",
            pageable = pageable
        )

        val dtoList = doctorPage.content.map { doctor ->
            val hospitalDoctor = doctorService.getFirstHospitalDoctorByDoctorId(doctor.id)
            DoctorDetailsResponse.from(doctor, hospitalDoctor)
        }

        return PageImpl(dtoList, pageable, doctorPage.totalElements)
        */

        return ResponseEntity.status(501).body(
            mapOf(
                "message" to "경력순 정렬은 아직 구현되지 않았습니다.",
                "implemented" to false
            )
        )
    }

    // 명성순 정렬 (미구현)
    // 예: http://localhost:8080/api/doctors/sort/reputation?page=0&size=10
    @GetMapping("/sort/reputation")
    fun getDoctorsSortedByReputation(
        @RequestParam(required = false) lat: Double?,
        @RequestParam(required = false) lng: Double?,
        pageable: Pageable
    ): ResponseEntity<Map<String, Any>> {
        /*
        // 추후 명성순 정렬 로직이 구현되면 아래 로직 활성화 예정
        val location = if (lat != null && lng != null) {
            val coordinate = Coordinate(lng, lat)
            val geometryFactory = GeometryFactory(PrecisionModel(), 4326)
            geometryFactory.createPoint(coordinate)
        } else null

        val doctorPage = doctorService.getDoctorsByFilters(
            keyword = null,
            location = location,
            sortBy = "reputation",
            pageable = pageable
        )

        val dtoList = doctorPage.content.map { doctor ->
            val hospitalDoctor = doctorService.getFirstHospitalDoctorByDoctorId(doctor.id)
            DoctorDetailsResponse.from(doctor, hospitalDoctor)
        }

        return PageImpl(dtoList, pageable, doctorPage.totalElements)
        */

        return ResponseEntity.status(501).body(
            mapOf(
                "message" to "명성순 정렬은 아직 구현되지 않았습니다.",
                "implemented" to false
            )
        )
    }

}
