package com.callrapport.controller.crawl

// 크롤러 관련 import
import com.callrapport.component.crawler.DiseaseCrawler // 질병 정보를 크롤링하는 클래스

// 서비스 관련 import
import com.callrapport.service.disease.DiseaseService // 질병 원본 데이터를 저장하는 서비스

// Spring 관련 import
import org.springframework.http.HttpStatus // HTTP 상태 코드 관련 클래스
import org.springframework.http.ResponseEntity // HTTP 응답을 생성하는 클래스
import org.springframework.web.bind.annotation.* // Spring의 REST 컨트롤러 관련 어노테이션

@RestController
@RequestMapping("/api/crawl/disease")
class DiseaseCrawlController(
    private val diseaseCrawler: DiseaseCrawler, // 질병 크롤러 주입
    private val diseaseService: DiseaseService // 질병 저장 서비스 주입
) {

    // 질병 링크(이름 + URL + 신체계통) 크롤링 API
    // ex) http://localhost:8080/api/crawl/disease/links
    @GetMapping("/links")
    fun crawlDiseaseLinks(): ResponseEntity<Any> {
        return try {
            // 크롤러를 이용해 모든 질병 링크 + 신체계통 정보 크롤링 수행
            val diseaseLinks = diseaseCrawler.crawlDiseaseLinks() 
            
            // 크롤링 결과를 Map 형태로 반환 (JSON 응답 구조로 가공)
            val response = diseaseLinks.map {
                mapOf(
                    "name" to it.name, // 질병명
                    "url" to it.url, // 질병 상세 URL
                    "bodySystem" to it.bodySystem // 해당 질병의 신체계통 (ex: 피부, 호흡기 등)
                )
            }
            // 가공된 결과를 HTTP 200 OK 응답으로 반환
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            // 크롤링 오류 발생 시, HTTP 500 에러와 함께 에러 메시지 반환
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "⚠️ ${e.message}"))
        }
    }

    // 질병 링크들을 기반으로 실제 질병 상세 정보 크롤링 및 DB 저장
    // ex) http://localhost:8080/api/crawl/disease/save-all
    @GetMapping("/save-all")
    fun saveAllDiseaseRaw(): ResponseEntity<Any> {
        return try {
            // 모든 질병 링크(이름 + URL + 신체계통)를 크롤링
            val diseaseLinks = diseaseCrawler.crawlDiseaseLinks()
            println("🔗 Number of disease links crawled: ${diseaseLinks.size}")

            // 각 질병 링크를 순회하면서 상세 정보 크롤링
            diseaseLinks.forEach { link ->
                // 질병명(name)과 URL을 기반으로 상세 페이지에서 정보 크롤링
                val details = diseaseCrawler.crawlDiseaseInfos(link.name, link.url)

                // 크롤링 결과 로그 출력
                println("🧬 Crawled disease details: $details")

                val name = details["name"] // 질병명
                val url = details["url"] // 상세 페이지 URL
                val symptoms = details["symptoms"] // 주요 증상
                
                // 필수 필드가 누락된 경우 저장 생략
                if (name == null || url == null || symptoms.isNullOrBlank()) {
                    println("❌ Skipping save: missing required fields - name: $name, url: $url, symptoms: $symptoms")
                    return@forEach // 해당 항목은 건너뛰고 다음 질병 저장을 시도
                }
                
                // 모든 필수 정보가 존재하면 DB에 저장 시도
                val saved = diseaseService.saveDiseaseRaw(
                    name = name, // 질병명 
                    url = url, // 질병 상세 페이지 URL
                    bodySystem = link.bodySystem, // 신체계통 정보
                    symptoms = symptoms // 주요 증상
                )
                
                // 저장 성공 로그 출력
                println("✅ Saved disease: ${saved.name}")
            }
            
            // 전체 크롤링 및 저장이 정상적으로 완료된 경우 HTTP 200 OK + 성공 메시지 응답
            ResponseEntity.ok("✅ All disease data has been successfully saved.")
        } catch (e: Exception) {
            // 전체 크롤링 또는 저장 과정 중 예외 발생 시 에러 로그 출력
            println("❗ Error during disease saving: ${e.message}")

            // HTTP 500 Internal Server Error + 에러 메시지 응답
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "⚠️ An error occurred while saving disease data: ${e.message}"))
        }
    }

    // 원본 질병 데이터를 기반으로 정제된 질병 데이터를 생성하는 API
    // ex) http://localhost:8080/api/crawl/disease/process-raw
    // !!! CHATGPT 토큰 소모되므로 꼭 신중하게 사용할 것
    // @GetMapping("/process-raw")
    // fun processRawDiseases(): ResponseEntity<String> {
    //     return try {
    //         // DiseaseService를 통해 원시 질병 데이터를 정제된 질병 데이터로 생성
    //         diseaseService.generateCleanDiseasesFromRaw()

    //         // 처리 성공 시, 200 OK 상태와 함께 성공 메시지 반환
    //         ResponseEntity.ok("All pending raw diseases have been processed successfully.")
    //     } catch (e: Exception) {
    //         // 처리 도중 예외 발생 시, 500 Internal Server Error 상태와 함께 에러 메시지 반환
    //         ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //             .body("An error occurred while processing raw diseases: ${e.message}")
    //     }
    // }
}
