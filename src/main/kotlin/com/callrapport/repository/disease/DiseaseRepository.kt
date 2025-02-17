package com.callrapport.repository.disease

import com.callrapport.model.disease.Disease
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DiseaseRepository : JpaRepository<Disease, Long> {
    fun findByNameKrContaining(nameKr: String): List<Disease> // 질병명(한국어)으로 검색
    fun findByNameEnContaining(nameEn: String): List<Disease> // 질병명(한국어)으로 검색
    fun findByDiseaseCode(diseaseCode: String): Disease? // 질병 코드로 검색
}
