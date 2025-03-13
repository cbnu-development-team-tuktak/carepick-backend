package com.callrapport.component.extractor

import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import org.jsoup.nodes.Element

@Component
class DoctorInfoExtractor {
    fun extractHospitalId(doc: Document): String? { // 병원 아이디 추출
        val hospitalElement = doc.selectFirst("div.hospital a.fc_blue")
        val hospitalUrl = hospitalElement?.attr("href")

        return hospitalUrl?.substringAfterLast("/")
    }

    fun extractProfileImage(doc: Document): String? { // 프로필 이미지 추출 
        return doc.selectFirst("div.thumb_img img")?.attr("src")
    }

    fun extractSpecialty(doc: Document): String? {  
        val specialtyElement = doc.selectFirst("div.clinic") // ✅ div.clinic 요소 선택 (기존 span 제거)
    
        if (specialtyElement == null) {
            println("⚠️ Specialty element not found in the page.")
        } else {
            println("✅ Specialty element found: ${specialtyElement.text()}")
        }
    
        // `진료과목`이라는 텍스트가 포함되어 있으므로, 해당 부분을 제거하고 반환
        return specialtyElement?.text()?.replace("진료과목", "")?.trim()
    }
    

    fun extractMainTreatment(doc: Document): String? { // 주요 진료 분야 추출
        return extractField(doc, "주요 진료 분야 ")
    }

    fun extractAcademicActivity(doc: Document): String? { // 학회활동 추출
        return extractField(doc, "학회활동")
    }

    fun extractEducationLicenses(doc: Document): List<String>? { // 학력/자격면허 추출 (여러 개 가능)
        return extractFieldList(doc, "학력/자격면허")
    }

    
    // 특정 필드 값을 `String?`으로 추출 (단일값)
    private fun extractField(doc: Document, title: String): String? {
        return extractFieldList(doc, title)?.joinToString(", ") // 리스트를 단일 문자열로 변환
    }

    // 특정 필드 값을 List<String>으로 추출 (여러 개 가능)   
    private fun extractFieldList(doc: Document, title: String): List<String>? {
        val element: Element? = doc.select("div.info_inner.career h3")
            .firstOrNull { it.text().contains(title) } // 정확한 제목 매칭

        val ulElement = element?.nextElementSibling()?.selectFirst("ul")

        // 리스트 항목을 선택
        val items = ulElement?.select("li.item span.desc") ?: emptyList()

        return if (items.isEmpty()) {
            println("⚠️ No valid data found for '$title'.")
            null
        } else {
            items.map { 
                it.html()
                    .replace("&nbsp;", "") // 불필요한 공백 제거
                    .replace("&amp;", "&") // HTML 엔티티 변환
                    .replace("<br>", ", ") // 줄바꿈을 콤마로 변환
                    .replace(Regex("^-\\s*"), "") // 문장 맨 앞의 '- ' 제거
                    .replace(Regex("-\\s*"), "")  // 모든 '-' 제거
                    .split(", ") // 마침표 앞에서 분리
                    .map { it.replace(".", "").trim() }  // 모든 마침표 제거 후 정리
                    .joinToString(", ") // 정리된 리스트를 다시 문자열로 변환
                    .replace(Regex("\\s*,\\s*"), ", ")  // 콤마 앞뒤 공백 정리
                    .trim().removeSuffix(",")  // 마지막 콤마 제거
            }.filter { it.isNotEmpty() } // 빈 값 제거
        }
    }
}
