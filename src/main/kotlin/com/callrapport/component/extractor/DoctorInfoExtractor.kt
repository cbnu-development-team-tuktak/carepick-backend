package com.callrapport.component.extractor

// Jsoup 관련 import (HTML 문서 파싱 및 데이터 추출을 위한 라이브러리)
import org.jsoup.nodes.Document // 웹 페이지의 전체 HTML 문서를 표현하는 클래스 (DOM 트리 구조)
import org.jsoup.nodes.Element // HTML 문서 내 개별 요소(태그)를 나타내는 클래스

// Spring 관련 import
import org.springframework.stereotype.Component // Spring의 컴포넌트로 등록

@Component
class DoctorInfoExtractor {
    // 병원 아이디 추출
    fun extractHospitalId(doc: Document): String? { 
        // 병원 정보를 포함하는 링크 요소 선택
        val hospitalElement = doc.selectFirst("div.hospital a.fc_blue")
        // 병원 URL 추출
        val hospitalUrl = hospitalElement?.attr("href")

        // URL의 마지막 부분(병원 ID) 반환
        return hospitalUrl?.substringAfterLast("/")
    }

    // 프로필 이미지 추출 
    fun extractProfileImage(doc: Document): String? { 
        // 프로필 이미지 URL 반환
        return doc.selectFirst("div.thumb_img img")?.attr("src")
    }

    // 진료과목 추출
    fun extractSpecialty(doc: Document): String? {  
        // div.clinis 요소 선택
        val specialtyElement = doc.selectFirst("div.clinic") 
        
        if (specialtyElement == null) { // 진료과목 요소가 없을 경우 경고 출력
            println("⚠️ Specialty element not found in the page.")
        } else { // 진료과목 요소가 있을 경우 로그 출력
            println("✅ Specialty element found: ${specialtyElement.text()}")
        }
    
        // "진료과목" 텍스트 제거 후 반환
        return specialtyElement?.text()?.replace("진료과목", "")?.trim()
    }
    
    // 경력 정보 추출
    fun extractCareer(doc: Document): String? {
        // 경력 정보를 포함한 요소 선택
        val careerElement = doc.selectFirst("div.doc_history:has(h4.tit:contains(경력)) ul.doctor_history li p.txt")

        return careerElement?.html()
            ?.replace("<br>", ", ") // <br> 태그를 쉼표로 변환
            ?.replace("<br/>", ", ")
            ?.replace("<br />", ", ")
            ?.trim() // 앞뒤 공백 제거 후 반환
    }
    
    // 학력 및 자격면허 정보 추출
    fun extractEducationLicenses(doc: Document): String? {
        // 학력/자격면허 정보를 포함한 요소 선택
        val educationElement = doc.selectFirst("div.doc_history:has(h4.tit:contains(학력/자격면허)) ul.doctor_history li p.txt")
    
        if (educationElement == null) { // 요소가 없을 경우 경고 출력
            println("⚠️ Education/Licenses element not found in the page.")
        } else { // 요소가 있을 경우 로그 출력
            println("✅ Education/Licenses element found: ${educationElement.html()}")
        }
    
        return educationElement?.html()
            ?.replace("<br>", ", ") // <br> 태그를 쉼표로 변환
            ?.replace("<br/>", ", ")
            ?.replace("<br />", ", ")
            ?.trim() // 앞뒤 공백 제거 후 반환
    }   
}
