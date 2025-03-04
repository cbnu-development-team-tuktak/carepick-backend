package com.callrapport.component.extractor

import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import org.jsoup.nodes.Element
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

@Component
class HospitalInfoExtractor {
    fun extractPhoneNumber(doc: Document): String? { // 전화번호 추출
        return doc.selectFirst("dl.tel dd a")?.text()
    }

    fun extractHomepage(doc: Document): String? { // 홈페이지 URL 추출
        return doc.selectFirst("dl.homepage dd a")?.attr("href")
    }

    fun extractAddress(doc: Document): String? { // 주소 추출
        return doc.selectFirst("dl.address dt:contains(주소) + dd")?.text()
    }

    fun extractSpecialties(doc: Document): String? { // 진료과목 추출
        return doc.select("dl.address dt:contains(진료과목) + dd span.keyword")
            .joinToString(" | ") { it.text() }
            .takeIf { it.isNotBlank() }
    }

    fun extractSubway(doc: Document): String? {
        val subwayElement = doc.selectFirst("dl.subway dd div.clear_g") ?: return null

        val line = subwayElement.select("strong").first()?.text()?.trim() ?: ""
        val stationText = subwayElement.ownText().trim() 
        val exitInfo = subwayElement.select("strong").last()?.text()?.trim() ?: ""

        return "$line $stationText $exitInfo".trim()
    }

    fun extractOperatingHours(doc: Document): String? {
        val operatingHours = mutableMapOf<String, String>()

        val timeInfoElement = doc.selectFirst("div.time_info") ?: return null
        val timeEntries = timeInfoElement.select("dl")

        for (entry in timeEntries) {
            val day = entry.selectFirst("dt")?.text()?.trim() ?: continue
            val time = entry.selectFirst("dd")?.text()?.replace("\n", " ")?.trim() ?: "운영 시간 없음"

            operatingHours[day] = time
        }
        return if (operatingHours.isNotEmpty()) {
            jacksonObjectMapper().writeValueAsString(operatingHours) // ✅ JSON 문자열로 변환
        } else {
            null
        }
    }
    
    fun extractAdditionalInfo(doc: Document, hospitalId: String): String? {
        val additionalInfoMap = mutableMapOf<String, Any>(
            "hospitalId" to hospitalId // ✅ 기본적으로 hospitalId 포함
        )
    
        // 항목 이름과 실제 JSON 필드명을 매핑
        val fieldMappings = mapOf(
            "24시간 문의 가능" to "open24Hours",
            "24시간 응급환자 진료" to "emergencyTreatment",
            "남여전문의 선택진료" to "maleFemaleDoctorChoice",
            "네트워크 병원" to "networkHospital",
            "무료 검진" to "freeCheckup",
            "역세권 위치" to "nearSubway",
            "연중무휴 진료" to "openAllYear",
            "일요일, 공휴일 진료" to "openOnSunday",
            "평일 야간 진료" to "nightShift",
            "협진시스템" to "collaborativeCare",
            "점심시간 없이 진료" to "noLunchBreak"
        )
    
        val specialItems = doc.select("ul.list_special li")
    
        for (item in specialItems) {
            val text = item.text().trim()
            val isActive = item.hasClass("on")
    
            // 매핑된 필드명으로 키 값 변경
            val mappedField = fieldMappings[text]
            if (mappedField != null) {
                additionalInfoMap[mappedField] = isActive
            }
        }
    
        return jacksonObjectMapper().writeValueAsString(additionalInfoMap)
    }
    
    
    fun extractDoctorIds(doc: Document): List<String> {
        val doctorIds = mutableListOf<String>()

        val doctorElements = doc.select("div.item_search.item_doctor a.link_award")

        for (element in doctorElements) {
            val href = element.attr("href")
            val doctorId = href.substringAfterLast("/")
            if (doctorId.isNotBlank()) {
                doctorIds.add(doctorId)
            }
        }
        
        return doctorIds
    }
}