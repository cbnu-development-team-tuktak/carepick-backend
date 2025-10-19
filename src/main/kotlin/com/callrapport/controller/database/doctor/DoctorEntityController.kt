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

    /**
     * 의사 통합 검색 및 필터링 API
     *
     * 기능: 의사 이름, 진료과, 정렬 기준 등 다양한 조건으로 의사를 검색하고 필터링합니다.
     *
     * @param keyword 검색할 의사 이름 (부분 일치). 예: "김민준"
     * @param specialtyNames 필터링할 진료과 이름 목록. 쉼표(,)로 구분하여 여러 개 요청 가능. 예: "내과,정형외과"
     * @param lat 사용자 현재 위치의 위도. 'distance' 정렬 시 필수. 예: 37.5665
     * @param lng 사용자 현재 위치의 경도. 'distance' 정렬 시 필수. 예: 126.9780
     * @param sortBy 정렬 기준. 아래의 값 중 하나를 사용해야 함. (기본값: "education")
     * - "education": 학력/자격면허 점수 높은 순
     * - "distance": 현재 위치에서 가까운 병원 소속 의사 순
     * (추후 "career", "reputation" 등 추가 예정)
     * @param pageable 페이지네이션 정보 (page, size). 예: page=0&size=10
     * @return 필터 및 정렬 조건에 맞는 의사 목록을 Page 형태로 반환
     */
    // 예시 1 (학력순 + 진료과 필터): http://localhost:8080/api/doctors/filter?sortBy=education&specialtyNames=내과,정형외과&page=0&size=10
    // 예시 2 (이름 검색 + 거리순): http://localhost:8080/api/doctors/filter?keyword=김의사&sortBy=distance&lat=37.5&lng=127.1&page=0&size=10
    // 예시 3 (진료과 필터만 사용): http://localhost:8080/api/doctors/filter?specialtyNames=피부과&page=0&size=10
    @GetMapping("/filter")
    fun getDoctorsByFilter(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) specialtyNames: List<String>?,
        @RequestParam(required = false) lat: Double?,
        @RequestParam(required = false) lng: Double?,
        @RequestParam(defaultValue = "education") sortBy: String,
        pageable: Pageable
    ): Page<DoctorDetailsResponse> {

        // 위도, 경도 값이 모두 있을 경우에만 Point 객체 생성
        val location = if (lat != null && lng != null) {
            val coordinate = Coordinate(lng, lat)
            val geometryFactory = GeometryFactory(PrecisionModel(), 4326)
            geometryFactory.createPoint(coordinate)
        } else null

        // 서비스를 통해 필터링된 의사 목록 조회
        val doctorPage = doctorService.getDoctorsByFilters(
            keyword = keyword,
            specialtyNames = specialtyNames,
            location = location,
            sortBy = sortBy,
            pageable = pageable
        )

        // 조회된 Doctor 엔티티 목록을 DTO 목록으로 변환
        val dtoList = doctorPage.content.map { doctor ->
            val hospitalDoctor = doctorService.getFirstHospitalDoctorByDoctorId(doctor.id)
            DoctorDetailsResponse.from(doctor, hospitalDoctor)
        }

        // 최종 결과를 Page 형태로 만들어 반환
        return PageImpl(dtoList, pageable, doctorPage.totalElements)
    }
}
