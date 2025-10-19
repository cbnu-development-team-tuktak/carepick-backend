package com.callrapport.controller.crawl

import com.callrapport.model.common.Image
// 크롤러 관련 import 
import com.callrapport.component.crawler.hospital.HospitalCrawler // 병원 정보를 크롤링하는 클래스
import com.callrapport.component.crawler.hospital.HospitalImageCrawler // 병원 이미지를 크롤링하는 클래스 
import com.callrapport.component.crawler.doctor.DoctorCrawler // 의사 정보를 크롤링하는 클래스

// 서비스 관련 import
import com.callrapport.service.map.AdministrativeRegionService
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

import com.callrapport.component.crawler.hospital.HospitalField
import java.net.URLEncoder

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
    private val administrativeRegionService: AdministrativeRegionService, // (수정) 행정구역 서비스 타입 명시

    // 리포지토리 
    private val specialtyRepository: SpecialtyRepository, // 진료과 정보 관리
    private val hospitalSpecialtyRepository: HospitalSpecialtyRepository, // 병원-진료과 관계 관리
    private val doctorRepository: DoctorRepository, // 의사 정보 관리
    private val hospitalDoctorRepository: HospitalDoctorRepository, // 병원-의사 관계 관리
    private val hospitalAdditionalInfoRepository: HospitalAdditionalInfoRepository, // 병원 추가 정보 관리
) {

    private val objectMapper = jacksonObjectMapper() // JSON 변환 객체 생성

    // 병원 목록(이름 + URL) 크롤링 API
    // 예: http://localhost:8080/api/crawl/hospital/hospital-links
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
            ResponseEntity.status(500)
                .body(listOf(mapOf("error" to "⚠️ ${e.message}")))
        }
    }

    /**
     * 도시/시 이름 키워드로 관련 모든 시/군/구의 병원 정보를 크롤링하고 저장합니다.
     *
     * 예시 URL:
     * - GET http://localhost:8080/api/crawl/hospital/by-city?keyword=청주시
     * - GET http://localhost:8080/api/crawl/hospital/by-city?keyword=충주시
     * - GET http://localhost:8080/api/crawl/hospital/by-city?keyword=서울특별시
     */
    @GetMapping("/by-city")
    fun crawlByCityKeyword(@RequestParam("keyword") cityKeyword: String): ResponseEntity<String> {
        println("▶️ API Request: Starting crawl for keyword '$cityKeyword'.")

        val targetSggs = administrativeRegionService.findSggsByKeyword(cityKeyword)

        if (targetSggs.isEmpty()) {
            val message = "⚠️ 키워드 '$cityKeyword'에 해당하는 시/군/구를 DB에서 찾을 수 없습니다."
            println("⚠️ Could not find any SGG in DB for keyword '$cityKeyword'.")
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message)
        }

        Thread {
            try {
                val targetRegionsText = targetSggs.joinToString { it.name }
                println("✅ Starting crawl for target regions: [${targetRegionsText}]")

                for (sgg in targetSggs) {
                    val sido = administrativeRegionService.findSidoBySgg(sgg)

                    if (sido == null) {
                        println("🚨 Could not find parent Sido for '${sgg.name}'. Skipping.")
                        continue
                    }

                    println("   - Starting crawl for region: '${sido.name} ${sgg.name}'...")

                    val hospitalLinks = hospitalCrawler.crawlHospitalLinks(
                        area1 = sido.name,
                        area2 = sgg.name
                    )

                    println("   - Found ${hospitalLinks.size} hospitals in '${sido.name} ${sgg.name}'. Starting to save details.")
                    
                    hospitalLinks.forEach { (name, url) ->
                        try {
                            // 상세 정보 저장 로직 시작
                            val hospitalId = extractHospitalIdFromUrl(url)
                            val hospitalInfo = hospitalCrawler.crawlHospitalInfos(name, url, HospitalField.values().toList())
                            
                            val additionalInfoJson = hospitalInfo["additional_info"]?.toString() ?: "{}"
                            val operatingHoursJson = hospitalInfo["operating_hours"]?.toString()
                            
                            val operatingHours: Map<String, Pair<String, String>>? = if (!operatingHoursJson.isNullOrBlank()) {
                                try {
                                    val parsedMap = objectMapper.readValue<Map<String, String>>(operatingHoursJson)
                                    parsedMap.mapValues { (_, value) ->
                                        val parts = value.split("~")
                                        val start = parts.getOrNull(0)?.trim() ?: "휴진"
                                        val end = parts.getOrNull(1)?.trim() ?: "휴진"
                                        start to end
                                    }
                                } catch (e: Exception) {
                                    println("❌ Failed to parse operating hours for hospital [$name]: ${e.message} (JSON: $operatingHoursJson)")
                                    null
                                }
                            } else {
                                null
                            }

                            val additionalInfo: Map<String, Any> = objectMapper.readValue(additionalInfoJson)
                            val specialties = hospitalInfo["specialties"] as? List<String> ?: emptyList()
                            val doctorUrlsJson = hospitalInfo["doctor_urls"]?.toString() ?: "[]"
                            val doctorUrls: List<Map<String, String>> = objectMapper.readValue(doctorUrlsJson)
                            
                            val doctorsData = mutableListOf<Map<String, String?>>()
                            doctorUrls.forEach { doctorData ->
                                val doctorName = doctorData["name"]
                                val doctorUrl = doctorData["url"]
                                val doctorId = doctorData["id"]

                                if (doctorName != null && doctorUrl != null && doctorId != null) {
                                    val doctorInfo = doctorCrawler.crawlDoctorInfos(doctorId, doctorName, doctorUrl)
                                    if (doctorInfo.isNotEmpty()) {
                                        doctorsData.add(doctorInfo)
                                    }
                                }
                            }
                            
                            val hospitalImages: List<Image> = hospitalImageCrawler.crawlHospitalImages(name)

                            hospitalService.saveHospital(
                                id = hospitalId,
                                name = name,
                                phoneNumber = hospitalInfo["phone_number"]?.toString(),
                                homepage = hospitalInfo["homepage"]?.toString(),
                                address = hospitalInfo["address"]?.toString() ?: "",
                                operatingHoursMap = operatingHours,
                                specialties = specialties,
                                url = url,
                                additionalInfo = additionalInfo,
                                doctors = doctorsData,
                                hospitalImages = hospitalImages
                            )
                            
                            println("   💾 Saved information for hospital [${name}].")
                            Thread.sleep(1000)
                            // 상세 정보 저장 로직 끝

                        } catch (e: Exception) {
                             println("   🚨 Error processing details for hospital [${name}]: ${e.message}")
                        }
                    }
                }
                println("🏁 Crawling task for keyword '$cityKeyword' has completed successfully.")
            } catch (e: Exception) {
                println("Fatal Error: A critical error occurred during the crawl for '$cityKeyword': ${e.message}")
            }
        }.start()

        return ResponseEntity.ok("'$cityKeyword' 키워드에 대한 크롤링이 백그라운드에서 시작되었습니다.")
    }

    /**
     * 광역자치단체(시/도) 이름 키워드로 해당 시/도에 속한 모든 시/군/구의 병원 정보를 크롤링하고 저장합니다.
     *
     * 예시 URL:
     * - GET http://localhost:8080/api/crawl/hospital/by-sido?keyword=울산
     */
    @GetMapping("/by-sido")
    fun crawlBySidoKeyword(@RequestParam("keyword") sidoKeyword: String): ResponseEntity<String> {
        println("▶️ API Request: Starting Sido crawl for keyword '$sidoKeyword'.")

        // 1. 키워드에 해당하는 모든 시/군/구(SGG) 목록을 조회 (새로운 서비스 함수 필요)
        // NOTE: 이 함수는 sidoKeyword가 "충청북도"라면 '청주시', '충주시', '제천시' 등의 모든 SGG를 반환해야 합니다.
        val targetSggs = administrativeRegionService.findSggsBySidoKeyword(sidoKeyword)

        if (targetSggs.isEmpty()) {
            val message = "⚠️ 키워드 '$sidoKeyword'에 해당하는 시/군/구를 DB에서 찾을 수 없습니다."
            println("⚠️ Could not find any SGGs in DB for sido keyword '$sidoKeyword'.")
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message)
        }

        Thread {
            try {
                val targetRegionsText = targetSggs.joinToString { it.name }
                println("✅ Starting crawl for target regions: [${targetRegionsText}]")

                // 2. 이후의 로직은 SGG 목록을 반복하며 크롤링하는 기존 로직과 동일합니다.
                for (sgg in targetSggs) {
                    // SGG 객체에서 부모 Sido를 찾는 기존 로직 사용
                    val sido = administrativeRegionService.findSidoBySgg(sgg) 

                    if (sido == null) {
                        println("🚨 Could not find parent Sido for '${sgg.name}'. Skipping.")
                        continue
                    }

                    println("   - Starting crawl for region: '${sido.name} ${sgg.name}'...")

                    val hospitalLinks = hospitalCrawler.crawlHospitalLinks(
                        area1 = sido.name,
                        area2 = sgg.name
                    )

                    println("   - Found ${hospitalLinks.size} hospitals in '${sido.name} ${sgg.name}'. Starting to save details.")
                    
                    hospitalLinks.forEach { (name, url) ->
                        try {
                            // --- 상세 정보 저장 로직 (이하 기존 로직과 동일) ---
                            val hospitalId = extractHospitalIdFromUrl(url)
                            val hospitalInfo = hospitalCrawler.crawlHospitalInfos(name, url, HospitalField.values().toList())
                            
                            val additionalInfoJson = hospitalInfo["additional_info"]?.toString() ?: "{}"
                            val operatingHoursJson = hospitalInfo["operating_hours"]?.toString()
                            
                            val operatingHours: Map<String, Pair<String, String>>? = if (!operatingHoursJson.isNullOrBlank()) {
                                try {
                                    val parsedMap = objectMapper.readValue<Map<String, String>>(operatingHoursJson)
                                    parsedMap.mapValues { (_, value) ->
                                        val parts = value.split("~")
                                        val start = parts.getOrNull(0)?.trim() ?: "휴진"
                                        val end = parts.getOrNull(1)?.trim() ?: "휴진"
                                        start to end
                                    }
                                } catch (e: Exception) {
                                    println("❌ Failed to parse operating hours for hospital [$name]: ${e.message} (JSON: $operatingHoursJson)")
                                    null
                                }
                            } else {
                                null
                            }

                            val additionalInfo: Map<String, Any> = objectMapper.readValue(additionalInfoJson)
                            val specialties = hospitalInfo["specialties"] as? List<String> ?: emptyList()
                            val doctorUrlsJson = hospitalInfo["doctor_urls"]?.toString() ?: "[]"
                            val doctorUrls: List<Map<String, String>> = objectMapper.readValue(doctorUrlsJson)
                            
                            val doctorsData = mutableListOf<Map<String, String?>>()
                            doctorUrls.forEach { doctorData ->
                                val doctorName = doctorData["name"]
                                val doctorUrl = doctorData["url"]
                                val doctorId = doctorData["id"]

                                if (doctorName != null && doctorUrl != null && doctorId != null) {
                                    val doctorInfo = doctorCrawler.crawlDoctorInfos(doctorId, doctorName, doctorUrl)
                                    if (doctorInfo.isNotEmpty()) {
                                        doctorsData.add(doctorInfo)
                                    }
                                }
                            }
                            
                            val hospitalImages: List<Image> = hospitalImageCrawler.crawlHospitalImages(name)

                            hospitalService.saveHospital(
                                id = hospitalId,
                                name = name,
                                phoneNumber = hospitalInfo["phone_number"]?.toString(),
                                homepage = hospitalInfo["homepage"]?.toString(),
                                address = hospitalInfo["address"]?.toString() ?: "",
                                operatingHoursMap = operatingHours,
                                specialties = specialties,
                                url = url,
                                additionalInfo = additionalInfo,
                                doctors = doctorsData,
                                hospitalImages = hospitalImages
                            )
                            
                            println("   💾 Saved information for hospital [${name}].")
                            Thread.sleep(1000)
                            // --- 상세 정보 저장 로직 끝 ---

                        } catch (e: Exception) {
                            println("   🚨 Error processing details for hospital [${name}]: ${e.message}")
                        }
                    }
                }
                println("🏁 Crawling task for sido keyword '$sidoKeyword' has completed successfully.")
            } catch (e: Exception) {
                println("Fatal Error: A critical error occurred during the crawl for '$sidoKeyword': ${e.message}")
            }
        }.start()

        return ResponseEntity.ok("'$sidoKeyword' 키워드에 대한 크롤링이 백그라운드에서 시작되었습니다.")
    }
    // 병원 전체 데이터 저장 (이름, 상세정보, 의사 목록 포함)
    // 예: http://localhost:8088/api/crawl/hospital/save-all
    @GetMapping("/save-all")
    fun saveAllHospitals(): ResponseEntity<String> {
        return try {
            // 병원 목록 크롤링 (이름과 URL)
            val hospitalLinks = hospitalCrawler.crawlHospitalLinks()

            hospitalLinks.forEach { (name, url) ->
                val hospitalId = extractHospitalIdFromUrl(url) // 병원 ID 추출
                val hospitalInfo = hospitalCrawler.crawlHospitalInfos(name, url, HospitalField.values().toList()) // 모든 필드를 활성화

                // 병원 부가 정보 JSON 파싱
                val additionalInfoJson = hospitalInfo["additional_info"]?.toString() ?: "{}"
                
                // 병원 정보에서 운영 시간 JSON 문자열 추출
                val operatingHoursJson = hospitalInfo["operating_hours"]?.toString()
                
                // 유효한 JSON 문자열이 있는 경우만 처리
                val operatingHours: Map<String, Pair<String, String>>? = if (!operatingHoursJson.isNullOrBlank()) {
                    try {
                        // JSON 문자열을 Map<String, Map<String, String>> 구조로 파싱
                        val parsed = objectMapper.readValue<Map<String, Map<String, String>>>(operatingHoursJson)

                        // 내부 value Map에서 "first"와 "second" 값을 추출하여 Pair로 변환
                        val splitMap = parsed.mapValues { (_, value) ->
                            val start = value["first"] ?: "휴진" // 시작 시간이 없으면 "휴진"으로 처리
                            val end = value["second"] ?: "휴진" // 종료 시간이 없으면 "휴진"으로 처리
                            start to end // (시작, 종료) 형태로 반환
                        }                
                        splitMap // 변환된 결과 반환
                    } catch (e: Exception) { // 파싱 도중 예외가 발생한 경우
                        // 파싱 에러 로그 송신
                        null // 실패 시 null 반환
                    }
                } else {
                    null
                }

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
                    operatingHoursMap = operatingHours, // 운영 시간
                    specialties = specialties, // 병원의 진료과 목록
                    url = url, // 병원 상세 페이지 URL
                    additionalInfo = additionalInfo, // 병원의 추가 정보
                    doctors = doctorsData, // 크롤링된 의사 정보 전달
                    hospitalImages = hospitalImages
                )
            }

            // 모든 병원 정보를 성공적으로 저장한 경우 응답 반환
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

