package com.callrapport.service.crawler

import com.callrapport.crawler.DiseaseCrawler
import org.springframework.stereotype.Service

@Service
class CrawlerService(
    private val diseaseCrawler: DiseaseCrawler
) {
    fun fetchDiseaseLinks(): List<String> {
        return try {
            diseaseCrawler.getDiseaseLinks()
        } catch (e: Exception) {
            println("⚠️ 질병 링크 크롤링 실패: ${e.message}")
            listOf("⚠️ 질병 링크 크롤링 실패: ${e.message}")
        }
    }

    fun fetchHospitalData() {
        println("🚀 병원 데이터 크롤링 로직 실행 중...")
    }

    fun fetchDoctorData() {
        println("🚀 의사 데이터 크롤링 로직 실행 중...")
    }
}
