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

    // 요일 전체 리스트
    private val weekDays = listOf("월", "화", "수", "목", "금", "토", "일")

    // 입력된 요일 텍스트("월~금", "토요일", "평일" 등)를 실제 요일 리스트로 변환
    fun parseDays(
        dayText: String // 변환할 요일 범위 또는 단일 요일이 포함된 문자열
    ): List<String> { // 변환된 요일 문자열 리스트 (예: ["월", "화", "수"])
        // "월~금", "금~화" 등 범위 형식이 포함된 경우
        if (dayText.contains("~")) {

            val (start, end) = dayText
                .split("~") // "~" 기호 기준으로 시작/종료 요일을 분리
                .map { it.trim().take(1) } // 각 항목에서 첫 글자만 추출 ("")
            
            // 시작 요일과 종료 요일의 인덱스를 요일 리스트에서 탐색
            val startIndex = weekDays.indexOf(start) // 시작 요일 인덱스 저장
            val endIndex = weekDays.indexOf(end) // 종료 요일 인덱스 저장
            
            // 유효한 인덱스일 경우
            if (startIndex != -1 && endIndex != -1) {
                // 예: "월~금" → 월, 화, 수, 목, 금
                return if (startIndex <= endIndex) {
                    weekDays.subList(startIndex, endIndex + 1)
                // 예: "금~화" → 금, 토, 일, 월, 화 (요일 순환) 
                } else {
                    weekDays.subList(startIndex, weekDays.size) + weekDays.subList(0, endIndex + 1)
                }
            }
        }

        return when {
            dayText.contains("평일") -> listOf("월", "화", "수", "목", "금") // "평일" → 월 ~ 금
            dayText.contains("주말") -> listOf("토", "일") // "주말" → 토, 일
            dayText.contains("월요일") -> listOf("월") // "월요일" → 월
            dayText.contains("화요일") -> listOf("화") // "화요일" → 화
            dayText.contains("수요일") -> listOf("수") // "수요일" → 수
            dayText.contains("목요일") -> listOf("목") // "목요일" → 목
            dayText.contains("금요일") -> listOf("금") // "금요일" → 금
            dayText.contains("토요일") -> listOf("토") // "토요일" → 토
            dayText.contains("일요일") -> listOf("일") // "일요일" → 일
            dayText.contains("공휴일") -> listOf("공휴일") // "공휴일" -> 공휴일
            else -> listOf(dayText) // 위 조건에 해당하지 않는 경우 원본 문자열 그대로 반환
        }
    }

    // 운영 시간 문자열("HH:mm - HH:mm")을 시작 시간과 종료 시간으로 분리
    fun parseStartAndEndTime(
        timeText: String // "10:00 - 18:00" 형식의 문자열
    ): Pair<String, String> {
        // "HH:mm - HH:mm" 형식에 대응하는 정규식
        val pattern = Regex("""\d{2}:\d{2}\s*-\s*\d{2}:\d{2}""")

        // 주어진 형식과 일치하는 경우
        return if (pattern.matches(timeText)) {
            // "-" 기준으로 분리 후 공백 제거
            val parts = timeText.split("-").map { it.trim() }
            
            // 시작 시간과 종료 시간 반환
            if (parts.size == 2) parts[0] to parts[1]

            // 분리가 제대로 이뤄지지 않은 경우 "휴진" 처리
            else "휴진" to "휴진"

        // 주어진 형식과 일치하지 않는 경우
        } else {
            "휴진" to "휴진"
        }
    }
    
    // 병원의 운영 시간을 추출
    fun extractOperatingHours(
        doc: Document // Jsoup의 Document 객체 (HTML 파싱 결과)
    ): Map<String, Pair<String, String>>? {
        // 요일 → (시작시간, 종료시간) 매핑
        val operatingHours = mutableMapOf<String, Pair<String, String>>()
        
        // 운영 시간 정보가 들어 있는 최상위 div 요소 선택
        val possibleDiv = doc.selectFirst("div.treatment_possibility_time div.possible") ?: return null
        // ul > li 형식으로 각 요일의 시간 정보를 선택
        val timeItems = possibleDiv.select("ul > li")
        
        // 시간 정보에 있는 요소들을 순회
        for (item in timeItems) {
            // 요일 텍스트 추출 (예: "월요일", "평일", "토~일" 등), 없으면 다음 항목으로 넘어감
            val dayText = item.selectFirst("span.day")?.text()?.trim() ?: continue

            // 시간 텍스트 추출 (예: "09:00 - 18:00"), 없으면 "휴진"으로 처리
            val timeText = item.selectFirst("span.time")?.text()?.trim() ?: "휴진"
    
            // 요일 텍스트를 실제 요일 리스트로 변환 (예: ["월", "화", ...])
            val days = parseDays(dayText)
            
            // logBroadcaster.sendLog("📅 운영시간 항목 발견 → 요일: '$dayText', 변환된 요일 목록: $days, 시간: '$timeText'")

            // 변환된 요일 리스트에 대해 반복 처리
            for (day in days) {
                // 시간 텍스트를 시작/종료 시간으로 분리 (예: "09:00" to "18:00")
                val (start, end) = parseStartAndEndTime(timeText)
                // logBroadcaster.sendLog("🕒 시간 파싱 완료 → $day: 시작='$start', 종료='$end'")
                
                // 결과를 요일 기준으로 맵에 저장 
                operatingHours[day] = start to end
            }
        }
        
        // 모든 요일 목록 정의 (공휴일 포함)
        val allDays = listOf("월", "화", "수", "목", "금", "토", "일", "공휴일")

        // 누락된 요일이 있다면 기본적으로 "휴진" 처리
        for (day in allDays) {
            if (day !in operatingHours) {
                // logBroadcaster.sendLog("⚠️ '$day' 요일 누락 → 휴진으로 처리됨")
                
                // 해당 요일이 없으면 "휴진"으로 처리
                operatingHours[day] = "휴진" to "휴진"
            }
        }
    
        // logBroadcaster.sendLog("✅ 최종 추출된 운영시간: $operatingHours")
        
        // 운영 시간이 하나라도 존재하면 반환, 아니면 null 반환
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