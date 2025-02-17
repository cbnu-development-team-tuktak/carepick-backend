package com.callrapport.crawler

import com.callrapport.model.disease.Disease
import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class DiseaseCrawler {
    fun getDiseaseInfos(url: String): Disease? {
        return try {
            val doc = Jsoup.connect(url).get()

            val nameKr = doc.selectFirst(".disease-title")?.text() ?: "이름 없음"
            val nameEn = doc.selectFirst(".disease-title-en")?.text()
            var definition= doc.selectFirst(".disease-definition")?.text()
            val symptoms = doc.selectFirst(".disease-symptoms")?.text()
            val causes = doc.selectFirst(".disease-causes")?.text()
            val diagnosis = doc.selectFirst(".disease-diagnosis")?.text()
            val treatment = doc.selectFirst(".disease-treatment")?.text()
            val prevention = doc.selectFirst(".disease-prevention")?.text()
            var diseaseCode = doc.selectFirst(".disease-code")?.text() ?: "N/A"

            Disease(
                nameKr = nameKr,
                nameEn = nameEn,
                definition = definition,
                symptoms = symptoms,
                causes = causes,
                diagnosis = diagnosis,
                treatment = treatment,
                prevention = prevention,
                diseaseCode = diseaseCode
            )
        } catch (e: Exception) {
            println("⚠️ 질병 정보 크롤링 실패: ${e.message}")
            null
        }
    }
}