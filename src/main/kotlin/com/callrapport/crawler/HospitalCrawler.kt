// package com.callrapport.service.crawler

// import com.callrapport.model.hospital.Hospital
// import org.jsoup.Jsoup
// import org.springframework.stereotype.Component

// @Component
// class HospitalCrawler {
//     fun getHospitalInfos(url: String): Hospital? {
//         return try {
//             val doc = Jsoup.connect(url).get()

//             val name = doc.selectFirst(".hospital-name")?.text() ?: "이름 없음"
//             val address = doc.selectFirst(".hospital-address")?.text()
//             val phone = doc.selectFirst(".hospital-phone")?.text()
//             val website = doc.selectFirst(".hospital-website")?.text()
//             val department = doc.selectFirst(".hospital-department")?.text()
//             val openingHours = doc.selectFirst(".hospital-opening-hours")?.text()

//             Hospital(
//                 name = name,
//                 address = address,
//                 phone = phone,
//                 website = website
//                 department = department,
//                 openingHours = openingHours
//             )
//         } catch (e: Exception) {
//             println("⚠️ 병원 정보 크롤링 실패 : ${e.message}")
//             null
//         }  
//     }
// }