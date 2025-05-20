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
}
