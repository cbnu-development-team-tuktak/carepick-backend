package com.callrapport.repository.university

import com.callrapport.model.university.Region
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RegionRepository : JpaRepository<Region, Long> {
    fun findByName(name: String): Region?
}
