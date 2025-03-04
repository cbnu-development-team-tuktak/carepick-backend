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

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
            val hospitalLinks = hospitalCrawler.crawlHospitalLinks() // 병원 목록 크롤링 실행

            // 링크에서 hospital ID를 추출하고 각 병원 정보를 크롤링
            val hospitalInfos = hospitalLinks.map { (name, url) ->
                // URL에서 ID를 추출
                val hospitalId = extractHospitalIdFromUrl(url)

                // 병원 정보 크롤링 결과를 MutableMap으로 변환하여 ID 추가
                val hospitalInfo = hospitalCrawler.crawlHospitalInfos(name, url).toMutableMap().apply {
                    this["id"] = hospitalId // hospitalId를 hospitalInfo에 추가
                }
                hospitalInfo
            }

            // 크롤링된 각 병원 정보를 DB에 저장
            hospitalInfos.forEach { hospitalInfo ->
                // 필수 값 확인 (null 체크)
                val hospitalId = hospitalInfo["id"] ?: throw IllegalArgumentException("Hospital ID is required")
                val name = hospitalInfo["name"] ?: throw IllegalArgumentException("Hospital name is required")
                val address = hospitalInfo["address"] ?: throw IllegalArgumentException("Hospital address is required")
                val url = hospitalInfo["url"] ?: throw IllegalArgumentException("Hospital URL is required")

                // 선택적 필드 처리: null을 허용하는 필드는 null이 들어올 수 있으므로 기본값 처리
                val phoneNumber = hospitalInfo["phone_number"] // null 가능
                val homepage = hospitalInfo["homepage"] // null 가능
                val operatingHours = hospitalInfo["operating_hours"] // null 가능
                val specialties = hospitalInfo["specialties"]?.split("|") ?: emptyList() // 기본값 처리
                val doctorIds = hospitalInfo["doctor_ids"]?.split(",") ?: emptyList() // 기본값 처리
                val additionalInfoJson = hospitalInfo["additional_info"] ?: "{}" // 기본값 처리

                // Hospital 정보를 DB에 저장
                val savedHospital = hospitalService.saveHospitalWithDetails(
                    id = hospitalId,
                    name = name,
                    phoneNumber = phoneNumber,
                    homepage = homepage,
                    address = address,
                    operatingHours = operatingHours,
                    specialties = specialties,
                    url = url,
                    doctors = doctorIds,
                    additionalInfo = jacksonObjectMapper().readValue(additionalInfoJson) // JSON 파싱
                )

                // specialties 저장
                if (specialties.isNotEmpty()) {
                    val specialtyEntities = specialties.map { specialtyName ->
                        // Specialty가 존재하는지 확인하고 없으면 새로 생성
                        val existingSpecialty = specialtyRepository.findByName(specialtyName)
                            ?: Specialty(name = specialtyName).also { specialtyRepository.save(it) }

                        HospitalSpecialty(specialty = existingSpecialty, hospital = savedHospital)
                    }
                    hospitalSpecialtyRepository.saveAll(specialtyEntities)
                }

                // doctors 저장
                if (doctorIds.isNotEmpty()) {
                    val doctorEntities = doctorRepository.findAllById(doctorIds)
                    val hospitalDoctors = doctorEntities.map { doctor ->
                        HospitalDoctor(hospital = savedHospital, doctor = doctor)
                    }
                    hospitalDoctorRepository.saveAll(hospitalDoctors)
                }

                // 추가 정보 저장 (optional)
                val additionalInfo = jacksonObjectMapper().readValue<HospitalAdditionalInfo>(additionalInfoJson)

                // 추가 정보의 hospitalId를 새로 할당
                val updatedAdditionalInfo = additionalInfo.copy(id = savedHospital.id) // `copy`로 새로운 객체 생성

                // 저장
                hospitalAdditionalInfoRepository.save(updatedAdditionalInfo)

            }

            ResponseEntity("All hospitals' info saved successfully", HttpStatus.OK)
        } catch (e: Exception) {
            println("Error occurred while saving hospitals: ${e.message}")
            e.printStackTrace()
            ResponseEntity.status(500).body("⚠️ Error occurred while saving hospitals: ${e.message}")
        }
    }

    // URL에서 hospitalId 추출하는 함수
    fun extractHospitalIdFromUrl(url: String): String {
        // URL에서 마지막 슬래시 뒤의 값을 ID로 추출
        return url.substringAfterLast("/")
    }


}
