package com.callrapport.controller.crawl

import com.callrapport.component.crawler.HospitalCrawler
import com.callrapport.component.crawler.DoctorCrawler
import com.callrapport.service.HospitalService
import com.callrapport.service.DoctorService
import com.callrapport.repository.common.SpecialtyRepository
import com.callrapport.repository.hospital.HospitalSpecialtyRepository
import com.callrapport.repository.doctor.DoctorRepository
import com.callrapport.repository.hospital.HospitalDoctorRepository
import com.callrapport.repository.hospital.HospitalAdditionalInfoRepository

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

@RestController
@RequestMapping("/api/crawl/hospital")
class HospitalCrawlController(
    private val hospitalCrawler: HospitalCrawler,
    private val doctorCrawler: DoctorCrawler,
    private val hospitalService: HospitalService,
    private val doctorService: DoctorService,
    private val hospitalSpecialtyRepository: HospitalSpecialtyRepository,
    private val doctorRepository: DoctorRepository,
    private val hospitalDoctorRepository: HospitalDoctorRepository,
    private val hospitalAdditionalInfoRepository: HospitalAdditionalInfoRepository,
    private val specialtyRepository: SpecialtyRepository
) {

    private val objectMapper = jacksonObjectMapper()

    @GetMapping("/hospital-links")
    fun crawlHospitalLinks(): ResponseEntity<List<Map<String, String>>> {
        return try {
            val hospitalLinks = hospitalCrawler.crawlHospitalLinks()
            val response = hospitalLinks.map { (name, url) -> mapOf("name" to name, "url" to url) }
            ResponseEntity(response, HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(listOf(mapOf("error" to "⚠️ ${e.message}")))
        }
    }

    @GetMapping("/save-all")
    fun saveAllHospitals(): ResponseEntity<String> {
        return try {
            val hospitalLinks = hospitalCrawler.crawlHospitalLinks()

            hospitalLinks.forEach { (name, url) ->
                val hospitalId = extractHospitalIdFromUrl(url)
                val hospitalInfo = hospitalCrawler.crawlHospitalInfos(name, url)

                // 병원 부가 정보 JSON 파싱
                val additionalInfoJson = hospitalInfo["additional_info"]?.toString() ?: "{}"
                val additionalInfo: Map<String, Any> = objectMapper.readValue(additionalInfoJson)

                // 병원 진료과 정보 처리
                val specialties = hospitalInfo["specialties"]?.toString()?.split("|")?.map { it.trim() } ?: emptyList()

                // 의사 URLs 처리
                val doctorUrlsJson = hospitalInfo["doctor_urls"]?.toString() ?: "[]"
                val doctorUrls: List<Map<String, String>>

                // doctorUrlsJson이 비어 있는지 확인
                if (doctorUrlsJson == "[]") {
                    println("No doctor URLs found for hospital: $name ($hospitalId)")
                    doctorUrls = emptyList()
                } else {
                    doctorUrls = try {
                        objectMapper.readValue(doctorUrlsJson)
                    } catch (e: Exception) {
                        println("Error parsing doctor URLs JSON for hospital: $name ($hospitalId). Error: ${e.message}")
                        emptyList()
                    }

                    println("Doctor URLs for hospital: $name ($hospitalId): ${doctorUrls.size} doctors found.")
                }

                val doctorsData = mutableListOf<Map<String, String?>>()

                // doctorUrls에서 의사 정보를 크롤링하고, doctorData 목록에 추가
                doctorUrls.forEach { doctorData ->
                    val doctorName = doctorData["name"]
                    val doctorUrl = doctorData["url"]
                    val doctorId = doctorData["id"] // doctorId도 미리 선언

                    // 의사 이름, URL, ID가 있는지 확인
                    if (doctorName == null || doctorUrl == null || doctorId == null) {
                        println("Skipping doctor due to missing name, url, or id: $doctorData")
                        return@forEach // 정보가 부족하면 건너뛰기
                    }

                    // URL이 올바른 형식인지 확인
                    if (doctorUrl.isBlank()) {
                        println("Invalid URL for doctor: $doctorName, URL: $doctorUrl")
                        return@forEach
                    }

                    // 크롤링 시작
                    println("Crawling data for doctor: $doctorName, ID: $doctorId, URL: $doctorUrl")

                    // 의사 크롤링 함수 호출
                    val doctorInfo = doctorCrawler.crawlDoctorInfos(doctorId, doctorName, doctorUrl)

                    if (doctorInfo.isNotEmpty() && doctorInfo["id"] != null) {
                        println("Doctor data successfully crawled: $doctorInfo") // doctorInfo 전체 출력
                        doctorsData.add(doctorInfo)
                    } else {
                        println("Failed to crawl doctor data for $doctorName, ID: $doctorId")
                    }
                }


                // 병원 정보 저장 (의사 정보도 함께 저장)
                hospitalService.saveHospital(
                    id = hospitalId,
                    name = name,
                    phoneNumber = hospitalInfo["phone_number"]?.toString(),
                    homepage = hospitalInfo["homepage"]?.toString(),
                    address = hospitalInfo["address"]?.toString() ?: "",
                    operatingHours = hospitalInfo["operating_hours"]?.toString(),
                    specialties = specialties,
                    url = url,
                    additionalInfo = additionalInfo,
                    doctors = doctorsData // 크롤링된 의사 정보 전달
                )
            }

            ResponseEntity.ok("All hospital info saved successfully")
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("⚠️ Error occurred while saving hospitals: ${e.message}")
        }
    }



    private fun extractHospitalIdFromUrl(url: String): String {
        return url.substringAfterLast("/")
    }
}
