package com.callrapport.repository.university

import com.callrapport.model.university.UniversityRank
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UniversityRankRepository : JpaRepository<UniversityRank, Int> {
    fun findByKrName(krName: String): UniversityRank?
    fun findByKrNameContaining(krName: String, pageable: Pageable): Page<UniversityRank>
    fun findByEnNameContaining(enName: String, pageable: Pageable): Page<UniversityRank>
    fun existsByKrName(krName: String): Boolean
    fun existsByEnName(enName: String): Boolean
}