package com.callrapport.controller.crawl

// 크롤러 관련 import
import com.callrapport.component.crawler.HospitalCrawler // DoctorCrawler: 의사 정보를 크롤링하는 클래스
import com.callrapport.service.HospitalService // 크롤링한 병원 데이터를 DB에 저장하는 서비스
import com.callrapport.model.hospital.HospitalAdditionalInfo
import com.callrapport.model.hospital.HospitalSpecialty // HospitalSpecialty 임포트 추가
import com.callrapport.model.common.Specialty // Specialty 임포트 추가
import com.callrapport.repository.common.SpecialtyRepository
import com.callrapport.model.hospital.HospitalDoctor // HospitalDoctor 임포트 추가
import com.callrapport.repository.hospital.HospitalSpecialtyRepository // 병원 진료과 레포지토리 임포트 추가
import com.callrapport.repository.doctor.DoctorRepository // 의사 레포지토리 임포트 추가
import com.callrapport.repository.hospital.HospitalDoctorRepository // 병원 의사 레포지토리 임포트 추가
import com.callrapport.repository.hospital.HospitalAdditionalInfoRepository // 병원 추가 정보 레포지토리 임포트 추가

// Spring 관련 import
import org.springframework.http.HttpHeaders // HTTP 요청/응답 헤더를 처리하는 클래스
import org.springframework.http.HttpStatus // HTTP 응답 상태 코드(200, 400, 500)를 정의하는 클래스
import org.springframework.http.ResponseEntity // HTTP 응답을 커스텀하기 위한 클래스 (응답 데이터 + 상태 코드 포함)
import org.springframework.web.bind.annotation.* // REST API 관련 어노테이션

import org.springframework.dao.OptimisticLockingFailureException

import com.fasterxml.jackson.databind.ObjectMapper

import com.fasterxml.jackson.module.kotlin.readValue
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/api/crawl/hospital")
class HospitalCrawlController(
    private val hospitalCrawler: HospitalCrawler,
    private val hospitalService: HospitalService, // 병원 정보 저장 서비스
    private val hospitalSpecialtyRepository: HospitalSpecialtyRepository, // 추가된 repository
    private val doctorRepository: DoctorRepository, // 추가된 repository
    private val hospitalDoctorRepository: HospitalDoctorRepository, // 추가된 repository
    private val hospitalAdditionalInfoRepository: HospitalAdditionalInfoRepository, // 추가된 repository
    private val specialtyRepository: SpecialtyRepository
) {
    // 병원 정보 크롤링 (병원명 + 프로필 링크 반환)
    @GetMapping("/links")
    fun getHospitalLinks(): ResponseEntity<List<Map<String, String>>> {
        return try {
            val hospitalLinks = hospitalCrawler.crawlHospitalLinks() // 병원 목록 크롤링 실행
            val response = hospitalLinks.map { (name, url) -> mapOf("name" to name, "url" to url) }
            ResponseEntity(response, HttpStatus.OK) // 크롤링 결과를 응답으로 반환
        } catch (e: Exception) {
            ResponseEntity.status(500).body(listOf(mapOf("error" to "⚠️ Error occurred: ${e.message}")))
        }
    }

    // 병원 상세 정보 크롤링
    @GetMapping("/infos")
    fun getHospitalInfos(): ResponseEntity<List<Map<String, String?>>> {
        return try {
            val hospitalLinks = hospitalCrawler.crawlHospitalLinks() // 병원 목록 크롤링 실행
            val hospitalInfos = hospitalLinks.map { (name, url) -> 
                hospitalCrawler.crawlHospitalInfos(name, url)
            }
            ResponseEntity(hospitalInfos, HttpStatus.OK) // 크롤링한 상세 정보를 반환
        } catch (e: Exception) {
            ResponseEntity.status(500).body(listOf(mapOf("error" to "⚠️ Error occurred: ${e.message}")))
        }
    }

    @GetMapping("/save-db")
    fun saveHospitalInfosToDB(): ResponseEntity<String> {
        return try {
            // 크롤링한 병원 정보 가져오기
            val hospitalLinks = hospitalCrawler.crawlHospitalLinks() // 병원 목록 크롤링 실행

            // 크롤링한 각 병원 정보를 DB에 저장
            hospitalLinks.forEach { (name, url) ->
                // URL에서 병원 ID 추출
                val hospitalId = extractHospitalIdFromUrl(url)

                // 병원 상세 정보 크롤링
                val hospitalInfo = hospitalCrawler.crawlHospitalInfos(name, url)

                // 추가 정보를 Map으로 변환 (JSON을 Map으로 변환하는 로직)
                val additionalInfo = hospitalInfo["additional_info"]?.let { info ->
                    try {
                        ObjectMapper().readValue(info as String, Map::class.java) as Map<String, Any>
                    } catch (e: Exception) {
                        emptyMap<String, Any>()
                    }
                }

                // specialties를 문자열로 받아 List<String>으로 변환
                val specialties = (hospitalInfo["specialties"] as? String)?.split(" | ") ?: emptyList()

                // 병원 정보 저장
                hospitalService.saveHospital(
                    id = hospitalId,
                    name = name,
                    phoneNumber = hospitalInfo["phone_number"] as? String,
                    homepage = hospitalInfo["homepage"] as? String,
                    address = hospitalInfo["address"] as? String ?: "",
                    operatingHours = hospitalInfo["operating_hours"] as? String,
                    specialties = specialties,  // specialties를 List<String>으로 변환하여 저장
                    url = url,
                    additionalInfo = additionalInfo
                )
            }
            // 모든 병원 정보 저장 완료 메시지 반환
            ResponseEntity("All hospital info saved successfully", HttpStatus.OK)
        } catch (e: Exception) {
            // 예외 발생 시 에러 메시지 반환
            ResponseEntity.status(500).body("⚠️ Error occurred while saving hospitals: ${e.message}")
        }
    }

    // URL에서 hospitalId 추출하는 함수
    fun extractHospitalIdFromUrl(url: String): String {
        // URL에서 마지막 슬래시 뒤의 값을 ID로 추출
        return url.substringAfterLast("/")
    }



}
