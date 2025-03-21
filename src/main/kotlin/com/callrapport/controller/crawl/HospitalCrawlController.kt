package com.callrapport.controller.crawl

import com.callrapport.model.common.Image
// 크롤러 관련 import 
import com.callrapport.component.crawler.hospital.HospitalCrawler // 병원 정보를 크롤링하는 클래스
import com.callrapport.component.crawler.hospital.HospitalImageCrawler // 병원 이미지를 크롤링하는 클래스 
import com.callrapport.component.crawler.doctor.DoctorCrawler // 의사 정보를 크롤링하는 클래스

// 서비스 관련 import
import com.callrapport.service.HospitalService // 병원 데이터를 저장하는 서비스
import com.callrapport.service.DoctorService // 의사 데이터를 저장하는 서비스

// 레포지토리 관련 import 
import com.callrapport.repository.common.SpecialtyRepository // 진료과 데이터를 관리하는 레포지토리
import com.callrapport.repository.hospital.HospitalSpecialtyRepository // 병원과 진료과 관계를 관리하는 레포지토리
import com.callrapport.repository.doctor.DoctorRepository // 의사 데이터를 관리하는 레포지토리
import com.callrapport.repository.hospital.HospitalDoctorRepository // 병원과 의사의 관계를 관리하는 레포지토리
import com.callrapport.repository.hospital.HospitalAdditionalInfoRepository // 병원의 추가 정보를 관리하는 레포지토리

// Spring 관련 import
import org.springframework.http.HttpStatus // HTTP 상태 코드 관련 클래스
import org.springframework.http.ResponseEntity // HTTP 응답을 생성하는 클래스
import org.springframework.web.bind.annotation.* // Spring의 REST 컨트롤러 관련 어노테이션

// JSON 변환 관련 import
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper // JSON 변환을 위한 Jackson 라이브러리
import com.fasterxml.jackson.module.kotlin.readValue // JSON 문자열을 객체로 변환하는 기능 제공

@RestController
@RequestMapping("/api/crawl/hospital")
class HospitalCrawlController(
    // 크롤러
    private val hospitalCrawler: HospitalCrawler, // 병원 크롤러
    private val hospitalImageCrawler: HospitalImageCrawler, // 병원 이미지 크롤러  
    private val doctorCrawler: DoctorCrawler, // 의사 크롤러

    // 서비스
    private val hospitalService: HospitalService, // 병원 서비스
    private val doctorService: DoctorService, // 의사 서비스

    // 레포지토리 
    private val specialtyRepository: SpecialtyRepository, // 진료과 정보 관리
    private val hospitalSpecialtyRepository: HospitalSpecialtyRepository, // 병원-진료과 관계 관리
    private val doctorRepository: DoctorRepository, // 의사 정보 관리
    private val hospitalDoctorRepository: HospitalDoctorRepository, // 병원-의사 관계 관리
    private val hospitalAdditionalInfoRepository: HospitalAdditionalInfoRepository, // 병원 추가 정보 관리
) {

    private val objectMapper = jacksonObjectMapper() // JSON 변환 객체 생성

    // 병원 목록(이름 + URL) 크롤링 API
    @GetMapping("/hospital-links")
    fun crawlHospitalLinks(): ResponseEntity<List<Map<String, String>>> {
        return try {
            // 병원 URL 목록을 크롤링하여 리스트 형태로 가져옴
            val hospitalLinks = hospitalCrawler.crawlHospitalLinks() 

            // 크롤링된 병원 데이터를 JSON 응답 형식으로 변환
            val response = hospitalLinks.map { (name, url) -> 
                mapOf("name" to name, "url" to url) // 병원 이름과 URL을 키-값 형태로 저장
            } 
            
            // HTTP 상태 코드 200(OK)와 함께 응답 반환
            ResponseEntity(response, HttpStatus.OK)
        } catch (e: Exception) {
            // 오류 발생 시 로그 출력 및 HTTP 500 오류 코드 반환
            ResponseEntity.status(500
                ).body(listOf(mapOf("error" to "⚠️ ${e.message}")))
        }
    }

    @GetMapping("/save-all")
    fun saveAllHospitals(): ResponseEntity<String> {
        return try {
            // 병원 목록 크롤링 (이름과 URL)
            val hospitalLinks = hospitalCrawler.crawlHospitalLinks()

            // 크롤링된 병원 목록을 순회하며 상세 정보를 가져옴
            hospitalLinks.forEach { (name, url) ->
                val hospitalId = extractHospitalIdFromUrl(url) // 병원 ID 추출
                val hospitalInfo = hospitalCrawler.crawlHospitalInfos(name, url) // 병원 상세 정보 크롤링

                // 병원 부가 정보 JSON 파싱
                val additionalInfoJson = hospitalInfo["additional_info"]?.toString() ?: "{}"
                
                // JSON을 Map으로 변환
                val additionalInfo: Map<String, Any> = objectMapper.readValue(additionalInfoJson)

                // 병원 진료과 정보를 리스트 형태로 변환
                val specialties = hospitalInfo["specialties"]?.toString()?.split("|")?.map { it.trim() } ?: emptyList()

                // 의사 정보(URL) JSON 처리
                val doctorUrlsJson = hospitalInfo["doctor_urls"]?.toString() ?: "[]"
                val doctorUrls: List<Map<String, String>>

                // 의사 URL 데이터가 비어 있는 경우 처리
                if (doctorUrlsJson == "[]") { // 의사 URL이 없을 경우 빈 리스트 반환
                    println("No doctor URLs found for hospital: $name ($hospitalId)")
                    doctorUrls = emptyList()
                } else {
                    // JSON 데이터를 리스트 형태로 변환
                    doctorUrls = try {
                        objectMapper.readValue(doctorUrlsJson)
                    } catch (e: Exception) {
                        println("Error parsing doctor URLs JSON for hospital: $name ($hospitalId). Error: ${e.message}")
                        emptyList() // 변환 실패 시 빈 리스트 반환
                    }
                    
                    // 크롤링된 의사 수 출력
                    println("Doctor URLs for hospital: $name ($hospitalId): ${doctorUrls.size} doctors found.")
                }
                
                // 크롤링된 의사 데이터를 저장할 리스트 생성
                val doctorsData = mutableListOf<Map<String, String?>>()

                // 병원에 등록된 의사 목록을 순회하며 크롤링 수행
                doctorUrls.forEach { doctorData ->
                    val doctorName = doctorData["name"] // 의사 이름 추출
                    val doctorUrl = doctorData["url"] // 의사 프로필 페이지 URL 추출
                    val doctorId = doctorData["id"] // 의사 ID 추출

                    // 의사 정보(이름, URL, ID(가 있는지 확인
                    if (doctorName == null || doctorUrl == null || doctorId == null) {
                        println("Skipping doctor due to missing name, url, or id: $doctorData")
                        return@forEach // 필수 정보가 없는 경우 해당 의사 데이터 건너뛰기
                    }

                    // URL이 올바른 형식인지 확인
                    if (doctorUrl.isBlank()) {
                        println("Invalid URL for doctor: $doctorName, URL: $doctorUrl")
                        return@forEach // URL이 비어있으면 크롤링 수행하지 않음
                    }

                    // 크롤링 시작 로그 출력
                    println("Crawling data for doctor: $doctorName, ID: $doctorId, URL: $doctorUrl")

                    // 의사 정보를 크롤링하는 함수 호출
                    val doctorInfo = doctorCrawler.crawlDoctorInfos(doctorId, doctorName, doctorUrl)

                    // 크롤링된 데이터가 정상적으로 존재하는 경우 리스트에 추가
                    if (doctorInfo.isNotEmpty() && doctorInfo["id"] != null) {
                        // 크롤링된 데이터 출력
                        println("Doctor data successfully crawled: $doctorInfo") 
                        // 크롤링된 의사 정보를 리스트에 저장
                        doctorsData.add(doctorInfo)
                    } else {
                        // 크롤링 실패 로그 출력
                        println("Failed to crawl doctor data for $doctorName, ID: $doctorId")
                    }
                }
                
                val hospitalImages: List<Image> = hospitalImageCrawler
                    .crawlHospitalImages(name)

                // 병원 정보 저장 (의사 정보 포함)
                hospitalService.saveHospital(
                    id = hospitalId, // 병원 ID
                    name = name, // 병원 이름
                    phoneNumber = hospitalInfo["phone_number"]?.toString(), // 병원 전화번호
                    homepage = hospitalInfo["homepage"]?.toString(), // 병원 홈페이지 URL
                    address = hospitalInfo["address"]?.toString() ?: "", // 병원 주소
                    operatingHours = hospitalInfo["operating_hours"]?.toString(), // 병원 운영 시간
                    specialties = specialties, // 병원의 진료과 목록
                    url = url, // 병원 상세 페이지 URL
                    additionalInfo = additionalInfo, // 병원의 추가 정보
                    doctors = doctorsData, // 크롤링된 의사 정보 전달
                    hospitalImages = hospitalImages
                )
            }

            // 모든 병원 정보를 성공적으로 저장한 경우 응답 반환ㄴ
            ResponseEntity.ok("All hospital info saved successfully")
        } catch (e: Exception) { // 예외 발생 시 오류 메시지 반환
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("⚠️ Error occurred while saving hospitals: ${e.message}")
        }
    }

    // 주어진 병원 URL에서 병원 ID를 추출
    private fun extractHospitalIdFromUrl(url: String): String {
        // URL의 마지막 '/' 이후에 나오는 문자열을 반환 (예: .../H001234567 → H0001234567)
        return url.substringAfterLast("/")
    }
}
