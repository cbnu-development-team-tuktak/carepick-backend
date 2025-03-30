package com.callrapport.service

// Model (엔티티) 관련 import
import com.callrapport.model.disease.DiseaseRaw // 질병 원본 데이터 엔티티
import com.callrapport.model.disease.DiseaseStatus // 질병 처리 상태 enum

// Repository (저장소) 관련 import
import com.callrapport.repository.disease.DiseaseRawRepository // 질병 원본 데이터 레포지토리

// 날짜/시간 관련 import
import java.time.LocalDateTime // 생성일 및 수정일 관리를 위한 LocalDateTime

// Spring 관련 import
import org.springframework.stereotype.Service // 서비스 클래스 어노테이션

@Service
class DiseaseService(
    private val diseaseRawRepository: DiseaseRawRepository // 질병 원본 데이터 저장소
) {
    // 질병 원본 정보를 저장
    fun saveDiseaseRaw(
        name: String, // 질병명
        url: String, // 질병 상세 페이지 URL
        bodySystem: String, // 신체계통
        symptoms: String, // 주요 증상
    ): DiseaseRaw {
        // 기존에 동일한 질병명이 있는지 확인
        val existing = diseaseRawRepository.findAll().find { it.name == name }

        // 기존 데이터가 존재하면 덮어쓰기
        val diseaseRaw = if (existing != null) {
            existing.copy(
                url = url, // URL 새로 덮어쓰기
                bodySystem = bodySystem, // 신체계통 갱신
                symptoms = symptoms, // 증상 새로 업데이트
                status = DiseaseStatus.PENDING, // PENDING으로 초기화
                updatedAt = LocalDateTime.now() // 수정 시간 갱신
            )
        } else {
            DiseaseRaw(
                name = name, // 질병명
                url = url, // 상세 페이지 URL
                bodySystem = bodySystem, // 신체계통
                symptoms = symptoms, // 주요 증상
                status = DiseaseStatus.PENDING // PENDING으로 초기화
            )
        }

        // 엔티티를 DB에 저장하고 반환
        return diseaseRawRepository.save(diseaseRaw)
    }

    // 처리 상태(status)를 기준으로 질병 리스트 조회
    fun getDiseasesByStatus(
        status: DiseaseStatus // 조회할 질병 처리 상태
    ): List<DiseaseRaw> {
        return diseaseRawRepository.findByStatus(status)
    }
}
