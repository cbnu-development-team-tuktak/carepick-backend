package com.callrapport.controller.test

// 크롤러 import
import com.callrapport.component.crawler.hospital.HospitalImageCrawler // 병원 이미지를 크롤링하는 컴포넌트

// Service 관련 import
import com.callrapport.service.DiseaseReasoningService // 질병 관련 GPT 서비스

// Spring 관련 import
import org.springframework.http.ResponseEntity // HTTP 응답 객체
import org.springframework.web.bind.annotation.GetMapping // GET 요청 처리 어노테이션
import org.springframework.web.bind.annotation.RequestMapping // URL 매핑 어노테이션
import org.springframework.web.bind.annotation.RequestParam // 쿼리 파라미터 추출 어노테이션
import org.springframework.web.bind.annotation.RestController // REST 컨트롤러 선언

import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/test")
class TestController(
    private val hospitalImageCrawler: HospitalImageCrawler, // 병원 이미지 크롤러
    private val diseaseReasoningService: DiseaseReasoningService // 증상 추출 서비스
) {
    // 병원명을 받아서 네이버 이미지 검색 결과에서 이미지 URL 리스트를 반환
    // ex) http://localhost:8080/api/test/place/images?hospitalName=베이드의원
    @GetMapping("/place/images")
    fun getHospitalImages(@RequestParam hospitalName: String): ResponseEntity<Map<String, Any>> {
        // 병원명을 이용해 네이버 이미지 검색 결과 크롤링 수행
        val imageSources = hospitalImageCrawler.crawlHospitalImages(hospitalName)
        
        // 이미지가 없는 경우 400 Bad Request 응답 반환
        if (imageSources.isEmpty()) {
            return ResponseEntity.badRequest().body(
                mapOf(
                    "error" to "Failed to retrieve hospital images.", // 에러 메시지
                    "hospitalName" to hospitalName // 요청에 사용된 병원 이름
                )
            )
        }

        // 크롤링 성공 시: 병원 이름과 이미지 URL 리스트를 응답으로 반환
        return ResponseEntity.ok(
            mapOf(
                "hospitalName" to hospitalName, // 요청된 병원 이름
                "status" to "Images extracted successfully.", // 처리 상태 메시지
                "imageSources" to imageSources // 크롤링된 이미지 URL 리스트
            )
        )
    }

    // 증상 추출 테스트
    // ex) http://localhost:8080/api/test/symptoms
    @GetMapping("/symptoms")
    fun testSymptomsExtraction(): ResponseEntity<Mono<List<String>>> {
        // 테스트할 질병 증상 문장 설정
        val sentence = """
            감기의 주요 증상은 콧물, 코막힘, 재채기, 인후통, 기침입니다. 주로 코, 인두부, 인후부 등 상기도에 국한됩니다. 발열은 유아와 소아가 성인에 비해 더 흔하게 발생합니다.
            인후통, 권태감, 발열이 시작된 후 하루나 이틀이 지나면 콧물, 코막힘, 기침이 발생합니다. 인후부의 동통(쑤시고 아픔), 건조감, 이물감도 느낄 수 있습니다.
            증상은 시작된 후 2~3일까지 최고로 심해진 후 1주 정도가 지나면 대부분 사라집니다. 일부 환자에게서는 증상이 2주까지 지속되기도 합니다. 감기로 인해 인후부가 손상되기도 하는데, 특히 건조한 계절에 감기로 손상된 인후부가 정상으로 회복이 되지 않으면 기침, 가래, 후두부의 이물감이 3주 이상 지속되기도 합니다. 흡연자의 경우 기침이 좀 더 심하고 오래 지속됩니다. 비염이 있는 경우 후비루 증후군이 지속되는 경우가 있고, 부비동염, 천식과 유사한 증상을 보이기도 합니다.
            원인 바이러스가 같을지라도 나이에 따라 발생 질환에 다소 차이가 있습니다. 파라인플루엔자 바이러스와 호흡기 세포 융합 바이러스는 소아에서 바이러스 폐렴, 후두 크루프(상기도 막힘), 세기관지염을 일으키지만, 성인에서는 감기만을 일으킵니다.
        """.trimIndent()

        // 증상 추출 함수 호출
        val result = diseaseReasoningService.extractSymptoms(sentence)

        // 결과를 HTTP 200 OK로 감싸서 반환
        return ResponseEntity.ok(result)
    }

    // 진료과 추출 테스트
    // ex) http://localhost:8080/api/test/specialties
    @GetMapping("/specialties")
    fun testSpecialtiesExtraction(): ResponseEntity<Mono<List<String>>> {
        // 실제 질병명 설정 (예: "감기")
        val diseaseName = "감기"

        // 환자의 증상 목록 설정 (extractSymptoms로 추출된 결과를 가정한 예시)
        val symptomsList = listOf("콧물", "코막힘", "재채기", "인후통", "기침", "발열", "권태감", "동통", "건조감", "이물감", "가래", "후두부 이물감", "후비루 증후군")

        // 진료과 추출 함수 호출
        val result = diseaseReasoningService.extractSpecialties(diseaseName, symptomsList)

        // 결과를 HTTP 200 OK로 감싸서 반환
        return ResponseEntity.ok(result)
    }
}
