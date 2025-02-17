package com.callrapport.service.crawler

import com.callrapport.crawler.*
import com.callrapport.repository.disease.DiseaseRepository
// import com.callrapport.repository.doctor.DoctorRepository
// import com.callrapport.repository.hospital.HospitalRepository

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CralwerSerivce( 
    private val crawler: Crawler,
    // private val hospitalCrawler: HospitalCrawler,
    // private val doctorCrawler: DoctorCrawler,
    private val diseaseCrawler: DiseaseCrawler,
    // private val hospitalRepository: HospitalRepository,
    // private val doctorRepository: DoctorRepository,
    private val diseaseRepository: DiseaseRepository
) {

    // // 병원 정보 크롤링 및 저장
    // @Async
    // @Transactional
    // fun crawlHospitals(listUrl: String) {
    //     val hospitalUrls = crawler.getUrlsFromListPage(listUrl, ".hospital-list a")
    //     for (url in hospitalUrls) {
    //         hospitalCrawler.getHospitalInfos(url)?.also { hospitalRepository.save(it) }
    //     }
    // }

    // // 의사 정보 크롤링 및 저장
    // @Async
    // @Transactional
    // fun crawlDoctors(listUrl: String) {
    //     val doctorUrls = crawler.getUrlsFromListPage(listUrl, ".doctor-list a")
    //     for (url in doctorUrls) {
    //         doctorCrawler.getDoctorInfos(url)?.also { doctorRepository.save(it) }
    //     }
    // }

    // 질병 정보 크롤링 및 저장
    @Async
    @Transactional
    fun crawlDiseases(listUrl: String) {
        val diseaseUrls = crawler.getUrlsFromListPage(listUrl, ".disease-list a")
        for (url in diseaseUrls) {
            diseaseCrawler.getDiseaseInfos(url)?.also { diseaseRepository.save(it) }
        }
    }
}   