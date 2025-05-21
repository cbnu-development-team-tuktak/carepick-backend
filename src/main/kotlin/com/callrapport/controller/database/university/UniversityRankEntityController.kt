package com.callrapport.controller.database.university

// DTO 관련 import
import com.callrapport.dto.UniversityRankDetailsResponse // 대학 랭킹 상세 정보를 담는 응답 DTO

// Service 계층 관련 import
import com.callrapport.service.university.UniversityRankService // 대학 랭킹 비즈니스 로직을 처리하는 서비스

// Spring 및 JPA 관련 import
import org.springframework.data.domain.Page // 페이지 단위 조회 결과를 표현하는 JPA 객체
import org.springframework.data.domain.Pageable // 페이지 요청 정보를 담는 JPA 객체
import org.springframework.http.ResponseEntity // HTTP 응답 본문 및 상태 코드를 포함하는 클래스
import org.springframework.web.bind.annotation.* // REST 컨트롤러와 관련된 어노테이션 모음 (RequestMapping, GetMapping 등)

@RestController
@RequestMapping("/api/university-ranks")
class UniversityRankEntityController(
    private val universityRankService: UniversityRankService
) {

    // 전체 대학 순위 조회 (페이지네이션)
    // 예: http://localhost:8080/api/hospitals/count
    @GetMapping
    fun getAllUniversityRanks(
        pageable: Pageable // 페이지네이션 정보를 포함한 객체
    ): Page<UniversityRankDetailsResponse> {
        val page = universityRankService.getAllUniversityRanks(pageable)
        return page.map { UniversityRankDetailsResponse.from(it) }
    }

    // 한국어 이름 기준 검색
    // 예: http://localhost:8080/api/universities/search/kr?keyword=서울&page=0&size=10
    @GetMapping("/search/kr")
    fun searchByKrName(
        @RequestParam keyword: String, // 검색할 한글 키워드
        pageable: Pageable // 페이지네이션 정보를 포함한 객체
    ): Page<UniversityRankDetailsResponse> {
        val page = universityRankService.searchByKrName(keyword, pageable)
        return page.map { UniversityRankDetailsResponse.from(it) }
    }

    // 영어 이름 기준 검색
    // 예: http://localhost:8080/api/universities/search/en?keyword=Seoul&page=0&size=10
    @GetMapping("/search/en")
    fun searchByEnName(
        @RequestParam keyword: String, // 검색할 영어 키워드
        pageable: Pageable // 페이지네이션 정보를 포함한 객체
    ): Page<UniversityRankDetailsResponse> {
        val page = universityRankService.searchByEnName(keyword, pageable)
        return page.map { UniversityRankDetailsResponse.from(it) }
    }
}
