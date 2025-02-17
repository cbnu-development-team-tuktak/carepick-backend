package com.callrapport.crawler

import com.callrapport.model.disease.Disease
import com.callrapport.utils.html.HtmlAnalyzer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component

@Component
class DiseaseCrawler(
    private val htmlAnalyzer: HtmlAnalyzer
) {
    fun getDiseaseLinks(): List<String> {
        return try {
            val url = "https://health.kdca.go.kr/healthinfo/biz/health/gnrlzHealthInfo/gnrlzHealthInfo/gnrlzHealthInfoMain.do"
            val htmlContent = htmlAnalyzer.fetchHtml(url)
            val document: Document = Jsoup.parse(htmlContent)

            val diseaseLinks = document.select("a[href^=javascript:fn_goView]")
                .filter { element -> element.selectFirst("i.xi-check-circle-o") != null }
                .map { element -> element.attr("href") }

            if (diseaseLinks.isEmpty()) {
                println("⚠️ No disease links found")
                emptyList()
            } else {
                println("✅ Found ${diseaseLinks.size} disease links")
                diseaseLinks
            }
        } catch (e: Exception) {
            println("⚠️ 질병 링크 크롤링 실패: ${e.message}")
            listOf("⚠️ 질병 링크 크롤링 실패: ${e.message}")
        }
    }

    fun getDiseaseInfos(url: String): Disease? {
        return try {
            val doc = Jsoup.connect(url).get()

            val nameKr = doc.selectFirst(".disease-title")?.text() ?: "이름 없음"
            val nameEn = doc.selectFirst(".disease-title-en")?.text()
            val definition = doc.selectFirst(".disease-definition")?.text()
            val symptoms = doc.selectFirst(".disease-symptoms")?.text()
            val causes = doc.selectFirst(".disease-causes")?.text()
            val diagnosis = doc.selectFirst(".disease-diagnosis")?.text()
            val treatment = doc.selectFirst(".disease-treatment")?.text()
            val prevention = doc.selectFirst(".disease-prevention")?.text()
            val diseaseCode = doc.selectFirst(".disease-code")?.text() ?: "N/A"

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
