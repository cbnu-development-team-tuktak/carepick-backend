package com.callrapport.controller.database.university

import com.callrapport.dto.UniversityRankDetailsResponse
import com.callrapport.service.university.UniversityRankService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/university-ranks")
class UniversityRankEntityController(
    private val universityRankService: UniversityRankService
) {

    // 전체 대학 순위 조회 (페이지네이션)
    @GetMapping
    fun getAllUniversityRanks(
        pageable: Pageable
    ): Page<UniversityRankDetailsResponse> {
        val page = universityRankService.getAllUniversityRanks(pageable)
        return page.map { UniversityRankDetailsResponse.from(it) }
    }

    // 한국어 이름 기준 검색
    @GetMapping("/search/kr")
    fun searchByKrName(
        @RequestParam keyword: String,
        pageable: Pageable
    ): Page<UniversityRankDetailsResponse> {
        val page = universityRankService.searchByKrName(keyword, pageable)
        return page.map { UniversityRankDetailsResponse.from(it) }
    }

    // 영어 이름 기준 검색
    @GetMapping("/search/en")
    fun searchByEnName(
        @RequestParam keyword: String,
        pageable: Pageable
    ): Page<UniversityRankDetailsResponse> {
        val page = universityRankService.searchByEnName(keyword, pageable)
        return page.map { UniversityRankDetailsResponse.from(it) }
    }
}
