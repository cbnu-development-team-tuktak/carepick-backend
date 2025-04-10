package com.callrapport.component.extractor

// Jsoup 관련 import (HTML 문서 파싱 및 데이터 추출을 위한 라이브러리)
import org.jsoup.nodes.Document // 웹 페이지의 전체 HTML 문서를 표현하는 클래스 (DOM 트리 구조)
import org.jsoup.nodes.Element // HTML 문서 내 개별 요소(태그)를 나타내는 클래스

// Spring 관련 import
import org.springframework.stereotype.Component // Spring의 컴포넌트로 등록

// JSON 변환 관련 
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper // JSON 변환을 위한 Jackson 라이브러리

import com.callrapport.component.log.LogBroadcaster // 로그 브로드캐스터

@Component
class HospitalInfoExtractor (
    private val logBroadcaster: LogBroadcaster // 로그 전송 컴포넌트
) {
    // 병원 전화번호 추출
    fun extractPhoneNumber(doc: Document): String? { 
        // 첫 번째 <a> 태그에서 전화번호 추출
        return doc.selectFirst("dl.tel dd a")?.text() 
    }

    // 병원의 홈페이지 URL을 추출
    fun extractHomepage(doc: Document): String? { 
        // 첫 번째 <a> 태그에서 href 속성값(홈페이지 URL) 추출
        return doc.selectFirst("dl.homepage dd a")?.attr("href")
    }

    // 병원의 주소를 추출하는 메서드
    fun extractAddress(doc: Document): String? { 
        // "주소"를 포함하는 dt 태그 다음 dd 태그에서 주소 텍스트 추출
        return doc.selectFirst("dl.address dt:contains(주소) + dd")?.text()
    }

    // 병원의 진료과목을 추출하는 메서드
    fun extractSpecialties(doc: Document): String? { 
        // "진료과목"을 포함하는 dt 태그 다음 dd 태그 내 span에서 진료과목 목록 추출
        return doc.select("dl.address dt:contains(진료과목) + dd span.keyword")
            .joinToString(" | ") { it.text() } // 여러 개의 진료과목을 " | "로 구분하여 문자열로 변환
            .takeIf { it.isNotBlank() } // 비어있지 않은 경우 반환
    }

    // 병원의 인근 지하철 정보를 추출하는 메서드
    fun extractSubway(doc: Document): String? {
        // 지하철 정보가 있는 div 요소 선택
        val subwayElement = doc.selectFirst("dl.subway dd div.clear_g") ?: return null

        // 첫 번째 <strong> 태그에서 지하철 호선 추출
        val line = subwayElement.select("strong").first()?.text()?.trim() ?: ""

        // 지하철역 이름 추출
        val stationText = subwayElement.ownText().trim() 

        // 마지막 <strong> 태그에서 출구 정보 추출
        val exitInfo = subwayElement.select("strong").last()?.text()?.trim() ?: ""

        // 지하철 호선 + 역 이름 + 출구 정보를 조합하여 반환
        return "$line $stationText $exitInfo".trim()
    }

    // "평일(월~금)", "월요일", "토요일" 등의 텍스트를 ["월", "화", ...] 식의 요일 리스트로 변환
    fun parseDays(dayText: String): List<String> {
        return when {
            dayText.contains("평일") -> listOf("월", "화", "수", "목", "금")
            dayText.contains("주말") -> listOf("토", "일")
            dayText.contains("월~금") -> listOf("월", "화", "수", "목", "금")
            dayText.contains("화~금") -> listOf("화", "수", "목", "금")
            dayText.contains("토~일") -> listOf("토", "일")
            dayText.contains("월요일") -> listOf("월")
            dayText.contains("화요일") -> listOf("화")
            dayText.contains("수요일") -> listOf("수")
            dayText.contains("목요일") -> listOf("목")
            dayText.contains("금요일") -> listOf("금")
            dayText.contains("토요일") -> listOf("토")
            dayText.contains("일요일") -> listOf("일")
            dayText.contains("공휴일") -> listOf("공휴일")
            else -> listOf(dayText) // 예외적으로 처리되지 않은 경우 원본 그대로 저장
        }
    }

    fun parseStartAndEndTime(timeText: String): Pair<String, String> {
        val pattern = Regex("""\d{2}:\d{2}\s*-\s*\d{2}:\d{2}""")
        return if (pattern.matches(timeText)) {
            val parts = timeText.split("-").map { it.trim() }
            if (parts.size == 2) parts[0] to parts[1]
            else "휴진" to "휴진"
        } else {
            "휴진" to "휴진"
        }
    }
    
    // 병원의 운영 시간을 추출하는 메서드 (JSON 대신 Map 반환)
    fun extractOperatingHours(doc: Document): Map<String, Pair<String, String>>? {
        val operatingHours = mutableMapOf<String, Pair<String, String>>()
    
        val possibleDiv = doc.selectFirst("div.treatment_possibility_time div.possible") ?: return null
        val timeItems = possibleDiv.select("ul > li")
    
        for (item in timeItems) {
            val dayText = item.selectFirst("span.day")?.text()?.trim() ?: continue
            val timeText = item.selectFirst("span.time")?.text()?.trim() ?: "휴진"
    
            val days = parseDays(dayText)
    
            // logBroadcaster.sendLog("📅 운영시간 항목 발견 → 요일: '$dayText', 변환된 요일 목록: $days, 시간: '$timeText'")
            for (day in days) {
                val (start, end) = parseStartAndEndTime(timeText)
                // logBroadcaster.sendLog("🕒 시간 파싱 완료 → $day: 시작='$start', 종료='$end'")
                operatingHours[day] = start to end
            }
        }
    
        val allDays = listOf("월", "화", "수", "목", "금", "토", "일", "공휴일")
        for (day in allDays) {
            if (day !in operatingHours) {
                // logBroadcaster.sendLog("⚠️ '$day' 요일 누락 → 휴진으로 처리됨")
                operatingHours[day] = "휴진" to "휴진"
            }
        }
    
        // logBroadcaster.sendLog("✅ 최종 추출된 운영시간: $operatingHours")
    
        return if (operatingHours.isNotEmpty()) operatingHours else null
    }
    
    

    // 병원의 추가 정보를 추출
    fun extractAdditionalInfo(doc: Document, hospitalId: String): String? {
        val additionalInfoMap = mutableMapOf<String, Any>(
            "hospitalId" to hospitalId // 병원 ID를 추가 정보에 포함
        )
    
        // HTML에서 제공하는 추가 정보와 JSON 필드명을 매핑
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
        
        // 추가 정보가 포함된 li 태그 선택
        val specialItems = doc.select("ul.list_special li")
    
        for (item in specialItems) {
            val text = item.text().trim() // 항목 이름 추출
            val isActive = item.hasClass("on") // 항목이 활성화되어 있는지 확인

            val mappedField = fieldMappings[text] // 매핑된 JSON 필드명 찾기
            if (mappedField != null) {
                additionalInfoMap[mappedField] = isActive // 매핑된 필드명으로 값 저장
            }
        }
    
        return jacksonObjectMapper().writeValueAsString(additionalInfoMap) // JSON 형식으로 변환하여 반환
    }
    
    // 병원에 소속된 의사들 링크를 추출
    fun extractDoctorUrls(doc: Document): List<Map<String, String>> {
        // 의사 정보를 저장할 리스트 생성
        val doctorList = mutableListOf<Map<String, String>>()
    
        // 의사 정보가 포함된 a 태그 선택
        val doctorElements = doc.select("div.item_search.item_doctor a.link_award")
        
        for (element in doctorElements) { 
            val rawUrl = element.attr("href") // 의사 상세 페이지 URL 추출
            // 의사 정보가 포함된 a 태그 선택
            val doctorUrl = if (rawUrl.startsWith("/")) "https://mobile.hidoc.co.kr$rawUrl" else rawUrl
            
            // 의사 이름 추출
            val doctorName = element.select("span.name .fw_b")?.text() ?: "No Name" // ✅ 의사 이름 추출
            
            // URL에서 의사 ID 추출
            val doctorId = doctorUrl.substringAfterLast("/") 
        
            // 디버깅 로그 출력
            println("🔍 Extracting doctor info: name=$doctorName, id=$doctorId, rawUrl=$rawUrl, fullUrl=$doctorUrl")
            
            // 의사 이름과 ID가 존재하는 경우만 리스트에 추가 
            if (doctorName.isNotBlank() && doctorId.isNotBlank()) {
                doctorList.add(
                    mapOf(
                        "id" to doctorId, // 의사 ID 저장
                        "name" to doctorName, // 의사 이름 저장
                        "url" to doctorUrl // 의사 상세 페이지 URL 저장
                    )
                )
            } else {
                // 데이터 누락 시 경고 출력
                println("⚠️ Skipping doctor due to missing data: name=$doctorName, id=$doctorId, rawUrl=$rawUrl, fullUrl=$doctorUrl")
            }
        }        
        return doctorList // 추출된 의사 정보 리스트 반환
    }    
}