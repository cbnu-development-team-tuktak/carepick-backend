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
import org.locationtech.jts.geom.Coordinate // 좌표 (경도, 위도) 표현용 클래스
import org.locationtech.jts.geom.GeometryFactory // 공간 객체 생성용 팩토리
import org.locationtech.jts.geom.Point // 위치 좌표를 나타내는 공간 데이터 타입
import org.locationtech.jts.geom.PrecisionModel // 좌표계 정밀도 설정 클래스

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

    // 의사 학력 기준 정렬
    // 예: http://localhost:8080/api/doctors/sort/education?lat=37.5&lng=127.1&page=0&size=10 (좌표 적용)
    // 예: http://localhost:8080/api/doctors/sort/education?page=0&size=10 (좌표 미적용)
    @GetMapping("/sort/education")
    fun getDoctorsSortedByEducation( 
        @RequestParam(required = false) keyword: String?, // 의사명
        @RequestParam(required = false) lat: Double?, // 위도
        @RequestParam(required = false) lng: Double?, // 경도
        pageable: Pageable // 페이지네이션 정보 (페이지 번호, 크기 등)
    ): Page<DoctorDetailsResponse> {
        // 위도와 경도가 모두 존재할 경우 좌표(Point) 객체 생성
        val location = if (lat != null && lng != null) {
            val coordinate = Coordinate(lng, lat) // 좌표 객체 생성 (경도, 위도 순)
            val geometryFactory = GeometryFactory(PrecisionModel(), 4326) // WGS84 좌표계 (SRID: 4326)
            geometryFactory.createPoint(coordinate) // Point 객체 생성
        } else null // 위도 또는 경도가 없으면 null

        // 의사 검색 서비스 호출 (학력 기준 정렬)
        val doctorPage = doctorService.getDoctorsByFilters(
            keyword = keyword, // 의사 이름 키워드 (부분 일치)
            location = location, // 위치 정보 (Point 좌표)
            sortBy = "education", // 학력 기준 정렬
            pageable = pageable // 페이지네이션 정보
        )

        // 검색된 위치 목록을 DTO 형태로 매핑
        val dtoList = doctorPage.content.map { doctor ->
            // 해당 의사와 연결된 첫 번째 병원 정보 조회
            val hospitalDoctor = doctorService.getFirstHospitalDoctorByDoctorId(doctor.id)
            // 의사 + 병원 정보를 포함한 응답 DTO 생성
            DoctorDetailsResponse.from(doctor, hospitalDoctor)
        }

        // 매핑된 DTO 리스트를 Page 형태로 변환하여 반환
        return PageImpl(dtoList, pageable, doctorPage.totalElements)
    }

    // 가까운 병원 소속순 정렬
    // 예: http://localhost:8080/api/doctors/sort/distance?lat=37.5&lng=127.1&page=0&size=10
    @GetMapping("/sort/distance")
    fun getDoctorsSortedByDistance(
        @RequestParam(required = false) keyword: String?, // 의사명
        @RequestParam(required = false) lat: Double?, // 위도
        @RequestParam(required = false) lng: Double?, // 경도
        pageable: Pageable // 페이지네이션 정보 (페이지 번호, 크기 등)
    ): Page<DoctorDetailsResponse> {
        // 위도와 경도가 모두 존재할 경우 좌표(Point) 객체 생성
        val location = if (lat != null && lng != null) {
            val coordinate = Coordinate(lng, lat) // 좌표 객체 생성 (경도, 위도 순)
            val geometryFactory = GeometryFactory(PrecisionModel(), 4326) // WGS84 좌표계 (SRID: 4326)
            geometryFactory.createPoint(coordinate) // Point 객체 생성
        } else null // 위도 또는 경도가 없으면 null
        
        // 의사 검색 서비스 호출 (거리 기준 정렬)
        val doctorPage = doctorService.getDoctorsByFilters(
            keyword = keyword, // 의사 이름 키워드 (부분 일치) 
            location = location, // 위치 정보 (Point 좌표)
            sortBy = "distance", // 거리 기준 정렬
            pageable = pageable // 페이지네이션 정보
        )

        // 검색된 의사 목록을 DTO 형태로 매핑 
        val dtoList = doctorPage.content.map { doctor ->
            // 해당 의사와 연결된 첫 번째 병원 정보 조회
            val hospitalDoctor = doctorService.getFirstHospitalDoctorByDoctorId(doctor.id)
            // 의사 + 병원 정보를 포함한 응답 DTO 생성
            DoctorDetailsResponse.from(doctor, hospitalDoctor)
        }

        // 매핑된 DTO 리스트를 Page 형태로 변환하여 반환
        return PageImpl(dtoList, pageable, doctorPage.totalElements)
    }

    // 경력순 정렬 (미구현)
    // 예: http://localhost:8080/api/doctors/sort/career?page=0&size=10
    @GetMapping("/sort/career")
    fun getDoctorsSortedByCareer(
        @RequestParam(required = false) keyword: String?, // 의사명
        @RequestParam(required = false) lat: Double?, // 위도
        @RequestParam(required = false) lng: Double?, // 경도
        pageable: Pageable // 페이지네이션 정보 (페이지 번호, 크기 등)
    ): ResponseEntity<Map<String, Any>> {
        /*
        // 추후 경력순 정렬 로직이 구현되면 아래 로직 활성화 예정
        val location = if (lat != null && lng != null) {
            val coordinate = Coordinate(lng, lat)
            val geometryFactory = GeometryFactory(PrecisionModel(), 4326)
            geometryFactory.createPoint(coordinate)
        } else null

        val doctorPage = doctorService.getDoctorsByFilters(
            keyword = keyword,
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

        // 경력순 정렬 미구현에 대한 응답 반환
        return ResponseEntity.status(501).body(
            mapOf(
                "message" to "경력순 정렬은 아직 구현되지 않았습니다.", // 사용자에게 전달할 안내 메시지
                "implemented" to false // 구현 여부 플래그 (false) 
            )
        )
    }

    // 명성순 정렬 (미구현)
    // 예: http://localhost:8080/api/doctors/sort/reputation?page=0&size=10
    @GetMapping("/sort/reputation")
    fun getDoctorsSortedByReputation(
        @RequestParam(required = false) keyword: String?, // 의사명
        @RequestParam(required = false) lat: Double?, // 위도
        @RequestParam(required = false) lng: Double?, // 경도
        pageable: Pageable // 페이지네이션 정보 (페이지 번호, 크기 등)
    ): ResponseEntity<Map<String, Any>> {
        /*
        // 추후 명성순 정렬 로직이 구현되면 아래 로직 활성화 예정
        val location = if (lat != null && lng != null) {
            val coordinate = Coordinate(lng, lat)
            val geometryFactory = GeometryFactory(PrecisionModel(), 4326)
            geometryFactory.createPoint(coordinate)
        } else null

        val doctorPage = doctorService.getDoctorsByFilters(
            keyword = keyword,
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

        // 명성순 정렬 미구현에 대한 응답 반환
        return ResponseEntity.status(501).body(
            mapOf(
                "message" to "명성순 정렬은 아직 구현되지 않았습니다.", // 사용자 안내 메시지
                "implemented" to false // 구현 여부 플래그 (false)
            )
        )
    }

}
