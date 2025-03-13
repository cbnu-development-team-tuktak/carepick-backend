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
    
    fun extractCareer(doc: Document): String? {
        val careerElement = doc.selectFirst("div.doc_history:has(h4.tit:contains(경력)) ul.doctor_history li p.txt")

        if (educationElement == null) {
            println("⚠️ Education/Licenses element not found in the page.")
        } else {
            println("✅ Education/Licenses element found: ${educationElement.html()}")
        }

        return educationElement?.html()
            ?.replace("<br>", ", ")
            ?.replace("<br/>", ", ")
            ?.replace("<br />", ", ")
            ?.trim()
    }
    
    fun extractEducationLicenses(doc: Document): String? {
        val educationElement = doc.selectFirst("div.doc_history:has(h4.tit:contains(학력/자격면허)) ul.doctor_history li p.txt")
    
        if (educationElement == null) {
            println("⚠️ Education/Licenses element not found in the page.")
        } else {
            println("✅ Education/Licenses element found: ${educationElement.html()}")
        }
    
        // ✅ <br> 태그를 쉼표(,)로 변환한 뒤 텍스트 추출
        return educationElement?.html()
            ?.replace("<br>", ", ")
            ?.replace("<br/>", ", ")
            ?.replace("<br />", ", ")
            ?.trim()
    }   
}
