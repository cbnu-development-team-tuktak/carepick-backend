package com.callrapport.service.disease

// Model (엔티티) 관련 import
import com.callrapport.model.disease.Symptom

// Repository (저장소) 관련 import
import com.callrapport.repository.disease.SymptomRepository

// Spring 관련 import
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class SymptomService(
    private val symptomRepository: SymptomRepository
) {

    // 모든 증상 정보를 페이지네이션으로 조회
    fun getAllSymptoms(pageable: Pageable): Page<Symptom> {
        return symptomRepository.findAll(pageable)
    }

    // ID를 기반으로 단일 증상 조회
    fun getSymptomById(id: Long): Symptom? {
        return symptomRepository.findById(id).orElse(null)
    }
}
