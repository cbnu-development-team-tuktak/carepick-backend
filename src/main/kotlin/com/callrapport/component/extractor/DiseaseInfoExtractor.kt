package com.callrapport.component.extractor

import org.jsoup.nodes.Document
import org.springframework.stereotype.Component

@Component
class DiseaeseInfoExtractor {
    // 기본 정보 추출
    fun extractOverview(doc: Document): String? = extractInfo(doc, "개요")
    fun extractDefinition(doc: Document): String? = extractInfo(doc, "정의")
    fun extractType(doc: Document): String? = extractInfo(doc, "종류")
    fun extractCause(doc: Document): String? = extractInfo(doc, "원인")
    fun extractSymptoms(doc: Document): String? = extractInfo(doc, "증상")
    fun extractDiagnosis(doc: Document): String? = extractInfo(doc, "진단")
    fun extractProgress(doc: Document): String? = extractInfo(doc, "경과")
    fun extractPathophysiology(doc: Document): String? = extractInfo(doc, "병태생리")

    // 치료 및 관리 정보 추출
    fun extractTreatment(doc: Document): String ?= extractInfo(doc, "치료")
    fun extractDrugTreatment(doc: Document): String ?= extractInfo(doc, "치료-약물 치료")
    fun extractNonDrugTreatment(doc: Document): String ?= extractInfo(doc, "치료-비약물 치료")
    fun extractSelfCare(doc: Document): String ?= extractInfo(doc, "자기 관리")
    fun extractSelfDiagnosis(doc: Document): String ?= extractInfo(doc, "자가 진단")
    fun extractWhenToVisitHospital(doc: Document): String ?= extractInfo(doc, "병원을 방문해야 하는 경우")

    // 연관 정보 추출
    fun extractRelatedDiseases(doc: Document): String ?= extractInfo(doc, "관련 질환")
    fun extractRelatedSymptoms(doc: Document): String ?= extractInfo(doc, "연관 증상")
    fun extractComplications(doc: Document): String ?= extractInfo(doc, "합병증")
    fun extractCustomMadeInfo(doc: Document): String ?= extractInfo(doc, "대상별 맞춤 정보")
    fun extractRelatedKeywords(doc: Document): String ?= extractInfo(doc, "연관 주제어")

    // 예방 및 참고 정보 추출
    fun extractPrevention(doc: Document): String ?= extractInfo(doc, "예방")
    fun extractFAQ(doc: Document): String ?= extractInfo(doc, "자주 하는 질문")
    fun extractReferences(doc: Document): String ?= extractInfo(doc, "참고 문헌")

    // 공통 정보 추출 함수
    private fun extractInfo(doc: Document, section: String): String? {
        val header = doc.select("h3:contains($section), p:containsOwn([$section])").firstOrNull()
        val sectionText = StringBuilder()

        header?.let {
            var nextElement = it.nextElementSibling()
            while (nextElement != null && nextElement.tagName() == "p") {
                sectionText.append(nextElement.text()).append("\n")
                nextElement = nextElement.nextElementSibling()
            }
        }
        return if (sectionText.isNotEmpty()) sectionText.toString().trim() else null
    }
}