package com.callrapport.service.university

// Model (엔티티) 관련 import
import com.callrapport.model.university.UniversityRank // 대학 랭킹 정보를 나타내는 엔티티

// Repository (저장소) 관련 import
import com.callrapport.repository.university.UniversityRankRepository // 대학 랭킹 정보를 조회하는 저장소

// Spring 및 JPA 관련 import
import org.springframework.data.domain.Page // 페이지 단위 조회 결과를 표현하는 JPA 객체
import org.springframework.data.domain.Pageable // 페이지 요청 정보를 담는 JPA 객체
import org.springframework.stereotype.Service // 해당 클래스를 Spring의 서비스 컴포넌트로 등록하는 어노테이션


@Service
class UniversityRankService(
    private val universityRankRepository: UniversityRankRepository
) {
    // 모든 대학 랭킹 정보를 페이지네이션으로 조회
    fun getAllUniversityRanks(
        pageable: Pageable // 페이지네이션 정보를 포함한 객체
    ): Page<UniversityRank> {
        return universityRankRepository.findAll(pageable)
    }

    // 입력한 한글 키워드를 포함하는 대학명을 페이지네이션으로 검색
    fun searchByKrName(
        keyword: String, // 검색할 한글 대학명 키워드 
        pageable: Pageable // 페이지네이션 정보를 포함한 객체
    ): Page<UniversityRank> {
        return universityRankRepository.findByKrNameContaining(keyword, pageable)
    }

    // 입력한 영어 키워드를 포함하는 대학명을 페이지네이션으로 검색
    fun searchByEnName(
        keyword: String, // 검색할 영어 대학명 키워드
        pageable: Pageable // 페이지네이션 정보를 포함한 객체
    ): Page<UniversityRank> {
        return universityRankRepository.findByEnNameContaining(keyword, pageable)
    }
}
