package com.callrapport.service.university

import com.callrapport.model.university.UniversityRank
import com.callrapport.repository.university.UniversityRankRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class UniversityRankService(
    private val universityRankRepository: UniversityRankRepository
) {

    fun getAllUniversityRanks(pageable: Pageable): Page<UniversityRank> {
        return universityRankRepository.findAll(pageable)
    }

    fun searchByKrName(keyword: String, pageable: Pageable): Page<UniversityRank> {
        return universityRankRepository.findByKrNameContaining(keyword, pageable)
    }

    fun searchByEnName(keyword: String, pageable: Pageable): Page<UniversityRank> {
        return universityRankRepository.findByEnNameContaining(keyword, pageable)
    }
}
