package com.callrapport.controller.crawl

// 서비스 관련 import
import com.callrapport.service.DoctorService
// DTO import (DTO 구조에 맞게 반환하기 위해 추가)
import com.callrapport.dto.DoctorDetailsResponse // <<< DTO import 확실히 추가
// 엔티티 import (DTO 변환에 필요)
import com.callrapport.model.doctor.Doctor
import com.callrapport.model.hospital.HospitalDoctor // HospitalDoctor 타입이 DTO 변환에 사용된다고 가정

// Spring 관련 import
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping 

@RestController
@RequestMapping("/api/crawl/doctor")
class FamousDoctorCrawlController(
    // 서비스
    private val doctorService: DoctorService // DoctorService 주입
) {

    /**
     * 명의 목록을 크롤링하는 테스트용 API 엔드포인트
     * 호출 URL: http://localhost:8080/api/crawl/doctor/famous-doctors
     */
    @GetMapping("/famous-doctors")
    fun crawlFamousDoctors(): ResponseEntity<List<Map<String, String>>> {
        return try {
            println("--- [API-Request] Starting famous doctor crawl process... ---")

            // DoctorService의 함수를 호출하도록 변경
            val famousDoctorsData = doctorService.crawlAndFetchFamousDoctors()

            println("--- [API-Request] Crawl finished successfully. Responding with data. ---")
            // 성공 시 HTTP 200(OK) 상태 코드와 함께 크롤링된 데이터 반환
            ResponseEntity.ok(famousDoctorsData)
        } catch (e: Exception) {
            println("--- [API-Request] An error occurred during the crawl process: ${e.message} ---")
            // 오류 발생 시 HTTP 500(Internal Server Error) 상태 코드와 함께 오류 메시지 반환
            val errorResponse = listOf(mapOf("error" to "⚠️ An error occurred during crawling: ${e.message}"))
            ResponseEntity.status(500).body(errorResponse)
        }
    }

    /**
     * [검수용] 크롤링된 데이터에서 모든 고유 진료과 목록을 분석하여 반환하는 API
     * 호출 URL: http://localhost:8080/api/crawl/doctor/analyze-specialties
     */
    @GetMapping("/analyze-specialties")
    fun analyzeSpecialties(): ResponseEntity<List<String>> {
        return try {
            println("--- [CONTROLLER-TEST] Received request for specialty analysis ---")

            // DoctorService의 분석 함수 호출
            val specialties = doctorService.analyzeCrawledSpecialties()

            println("--- [CONTROLLER-TEST] Analysis finished. Returning ${specialties.size} unique specialties. ---")
            ResponseEntity.ok(specialties)
        } catch (e: Exception) {
            println("--- [CONTROLLER-TEST] Analysis failed with an error: ${e.message} ---")
            ResponseEntity.status(500).body(emptyList())
        }
    }

    /**
     * [최종 반영] 명의 정보를 크롤링하고 DB에 업데이트한 후, 업데이트된 의사 목록을 DTO 형태로 반환하는 API
     * 호출 URL: http://localhost:8080/api/crawl/doctor/update-famous-status
     * HTTP 메서드: GET (사장님의 지시에 따름)
     */
    @GetMapping("/update-famous-status") 
    fun updateFamousDoctors(): List<DoctorDetailsResponse> { 
        // try-catch 블록 제거, 예외 처리는 Spring이 담당하도록 위임

        println("--- [API-Request] Starting famous doctor DB update ---")

        // 1. 서비스의 업데이트 함수 호출 (List<Doctor> 반환)
        val updatedDoctors = doctorService.updateFamousDoctors()

        // 2. Doctor 엔티티 목록을 DoctorDetailsResponse DTO 목록으로 변환
        val dtoList = updatedDoctors.map { doctor ->
            // DTO 변환을 위해 소속 병원 정보(HospitalDoctor)를 가져와야 함을 가정
            val hospitalDoctor = doctorService.getFirstHospitalDoctorByDoctorId(doctor.id) 
            DoctorDetailsResponse.from(doctor, hospitalDoctor)
        }

        println("--- [API-Request] DB update finished. Total ${dtoList.size} doctors updated. ---")
        return dtoList // DTO 리스트 직접 반환
    }
}