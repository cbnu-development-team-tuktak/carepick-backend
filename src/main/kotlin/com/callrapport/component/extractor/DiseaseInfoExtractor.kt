package com.callrapport.component.extractor

// Jsoup 관련 import (HTML 문서 파싱 및 데이터 추출을 위한 라이브러리)
import org.jsoup.nodes.Document // 웹 페이지의 전체 HTML 문서를 표현하는 클래스 (DOM 트리 구조)

// Spring 관련 import
import org.springframework.stereotype.Component // Spring의 컴포넌트로 등록

@Component
class DiseaseInfoExtractor {
    // 질병의 기본 정보 추출

    // 개요 정보 추출
    fun extractOverview(doc: Document): String? = extractInfo(doc, "개요")
    
    // 정의 정보 추출
    fun extractDefinition(doc: Document): String? = extractInfo(doc, "정의")

    // 종류 정보 추출
    fun extractType(doc: Document): String? = extractInfo(doc, "종류")

    // 원인 정보 추출
    fun extractCause(doc: Document): String? = extractInfo(doc, "원인")

    // 증상 정보 추출
    fun extractSymptoms(doc: Document): String? = extractInfo(doc, "증상")
    
    // 진단 정보 추출
    fun extractDiagnosis(doc: Document): String? = extractInfo(doc, "진단")

    // 경과 정보 추출
    fun extractProgress(doc: Document): String? = extractInfo(doc, "경과")

    // 병태생리 정보 추출
    fun extractPathophysiology(doc: Document): String? = extractInfo(doc, "병태생리")


    // 질병의 치료 및 관리 정보 추출

    // 치료 방법 추출
    fun extractTreatment(doc: Document): String ?= extractInfo(doc, "치료")

    // 약물 치료 정보 추출
    fun extractDrugTreatment(doc: Document): String ?= extractInfo(doc, "치료-약물 치료")

    // 비약물 치료 정보 추출
    fun extractNonDrugTreatment(doc: Document): String ?= extractInfo(doc, "치료-비약물 치료")

    // 자기 관리 방법 추출
    fun extractSelfCare(doc: Document): String ?= extractInfo(doc, "자기 관리")

    // 자가 진단 방법 추출
    fun extractSelfDiagnosis(doc: Document): String ?= extractInfo(doc, "자가 진단")

    // 병원 방문 필요 여부 추출
    fun extractWhenToVisitHospital(doc: Document): String ?= extractInfo(doc, "병원을 방문해야 하는 경우")

    // 연관 정보 추출

    // 관련 질환 정보 추출
    fun extractRelatedDiseases(doc: Document): String ?= extractInfo(doc, "관련 질환")

    // 연관 증상 정보 추출
    fun extractRelatedSymptoms(doc: Document): String ?= extractInfo(doc, "연관 증상")

    // 합병증 정보 추출
    fun extractComplications(doc: Document): String ?= extractInfo(doc, "합병증")

    // 대상별 맞춤 정보 추출
    fun extractCustomMadeInfo(doc: Document): String ?= extractInfo(doc, "대상별 맞춤 정보")

    // 연관 주제어 추출
    fun extractRelatedKeywords(doc: Document): String ?= extractInfo(doc, "연관 주제어")

    // 예방 및 참고 정보 추출

    // 예방 방법 추출
    fun extractPrevention(doc: Document): String ?= extractInfo(doc, "예방")

    // 자주 하는 질문 추출
    fun extractFAQ(doc: Document): String ?= extractInfo(doc, "자주 하는 질문")

    // 참고 문헌 추출
    fun extractReferences(doc: Document): String ?= extractInfo(doc, "참고 문헌")

    // 공통 정보 추출 함수
    private fun extractInfo(
        doc: Document, // Jsouo의 Document 객체 (웹 페이지의 전체 HTML 문서)
        section: String // 추출할 섹션 제목
    ): String? {
        // 섹션 제목 찾기
        val header = doc.select("h3:contains($section), p:containsOwn([$section])").firstOrNull()
        val sectionText = StringBuilder()

        header?.let {
            var nextElement = it.nextElementSibling() // 다음 요소 가져오기
            while (nextElement != null && nextElement.tagName() == "p") { // p 태그 내용 가져오기
                sectionText.append(nextElement.text()).append("\n") // 줄바꿈 추가하여 내용 저장
                nextElement = nextElement.nextElementSibling() // 다음 요소로 이동
            }
        }
        
        // 내용이 있으면 반환, 없으면 null 반환
        return if (sectionText.isNotEmpty()) sectionText.toString().trim() else null
    }
}