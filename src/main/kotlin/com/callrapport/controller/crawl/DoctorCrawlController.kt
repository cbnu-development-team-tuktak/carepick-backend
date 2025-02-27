package com.callrapport.controller.crawl

// 크롤러 관련 import
import com.callrapport.component.crawler.DoctorCrawler // DoctorCrawler: 의사 정보를 크롤링하는 클래스
import com.callrapport.service.DoctorService // 크롤링한 의사 데이터를 DB에 저장하는 서비스

// Spring 관련 import
import org.springframework.http.HttpHeaders // HTTP 요청/응답 헤더를 처리하는 클래스
import org.springframework.http.HttpStatus // HTTP 응답 상태 코드(200, 400, 500)를 정의하는 클래스
import org.springframework.http.ResponseEntity // HTTP 응답을 커스텀하기 위한 클래스 (응답 데이터 + 상태 코드 포함)
import org.springframework.web.bind.annotation.* 

@RestController
@RequestMapping("/api/crawl/doctor")
class DoctorCrawlController(
    private val doctorCrawler: DoctorCrawler,
    private val doctorService: DoctorService // 의사 정보 저장 서비스
) {
    // 의사 정보 크롤링 (성명 + 프로필 링크 반환)
    @GetMapping("/links")
    fun getDoctorLinks(): ResponseEntity<List<Map<String, String>>> {
        return try {
            val doctorLinks = doctorCrawler.crawlDoctorLinks() // 의사 목록 크롤링 실행
            val response = doctorLinks.map { (name, url) -> mapOf("name" to name, "url" to url) }
            ResponseEntity(response, HttpStatus.OK) // 크롤링 결과를 응답으로 반환
        } catch (e: Exception) {
            ResponseEntity.status(500).body(listOf(mapOf("error" to "⚠️ Error occurred: ${e.message}")))
        }
    }

    // 의사 상세 정보 크롤링 
    @GetMapping("/infos")
    fun getDoctorInfos(): ResponseEntity<List<Map<String, String?>>> {
        return try {
            val doctorLinks = doctorCrawler.crawlDoctorLinks() // 의사 목록 크롤링 실행
            val doctorInfos = doctorLinks.map { (name, url) -> 
                doctorCrawler.crawlDoctorInfos(name, url) 
            }
            ResponseEntity(doctorInfos, HttpStatus.OK) // 크롤링한 상세 정보를 반환
        } catch (e: Exception) {
            ResponseEntity.status(500).body(listOf(mapOf("error" to "⚠️ Error occurred: ${e.message}")))
        }
    }

    // 크롤링한 의사 정보를 DB에 저장
    @GetMapping("/save-db")
    fun saveDoctorInfosToDB(): ResponseEntity<String> {
        return try {
            val doctorLinks = doctorCrawler.crawlDoctorLinks() // 의사 목록 크롤링 실행
            val doctorInfos = doctorLinks.map { (name, url) ->
                doctorCrawler.crawlDoctorInfos(name, url)
            }
            
            // 크롤링한 각 의사 정보를 DB에 저장
            doctorInfos.forEach { doctorInfo ->
                doctorService.saveDoctorWithDetails(
                    id = doctorInfo["id"]!!, // 의사 ID
                    name = doctorInfo["name"]!!, // 의사 이름
                    profileImage = doctorInfo["profileImage"], // 프로필 이미지
                    educationLicenses = doctorInfo["educationLicenses"]?.split(", ") ?: emptyList(), // 자격면허
                    hospitalId = doctorInfo["hospitalId"], // 병원 ID
                    specialtyName = doctorInfo["specialty"] // 진료과 
                )
            }
            ResponseEntity("All doctors' info saved successfully", HttpStatus.OK) // 저장 완료 메시지 반환
        } catch (e: Exception) {
            ResponseEntity.status(500).body("⚠️ Error occurred while saving doctors: ${e.message}")
        }
    }
} 