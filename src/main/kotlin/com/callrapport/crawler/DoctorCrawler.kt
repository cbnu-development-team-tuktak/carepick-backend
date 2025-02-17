package com.callrapport.crawler

import com.callrapport.model.doctor.Doctor
import com.callrapport.model.hospital.Hospital
import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class DoctorCrawler {
    fun getDoctorInfos(url: String): Doctor? {
        return try {
            val doc = Jsoup.connect(url).get()

            val name = doc.selectFirst(".doctor-name")?.text() ?: "이름 없음"
            val specialty = doc.selectFirst(".doctor-specialty")?.text() ?: "전문의 정보 없음"
            val gender = doc.selectFirst(".doctor-gender")?.text() ?: "성별 정보 없음"

            val experience = doc.selectFirst(".doctor-experience")?.text()?.toIntOrNull() ?: 0
            val education = doc.selectFirst("doctor-education")?.text() ?: "학력 정보 없음"
            val career = doc.selectFirst(".doctor-career")?.text() ?: "경력 정보 없음"
            val certifications = doc.selectFirst(".doctor-certifications")?.text() ?: "자격 정보 없음"
            val hospitalName = doc.selectFirst(".doctor-hospital")?.text() ?: "소속 병원 없음"

            var hospital = Hospital(name = hospitalName)

            Doctor(
                name = name, 
                specialty = specialty,
                gender = gender,
                experience = experience,
                education = education,
                career = career,
                certifications = certifications,
                hospital = hospital
            )
        } catch (e: Exception) {
            println("⚠️ 의사 정보 크롤링 실패 : ${e.message}")
            null
        }
    }
}