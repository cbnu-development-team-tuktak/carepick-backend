package com.callrapport.repository.university

import com.callrapport.model.university.UniversityRankRegion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UniversityRankRegionRepository : JpaRepository<UniversityRankRegion, Long>
