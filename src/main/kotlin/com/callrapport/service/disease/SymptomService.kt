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

    fun getSymptomsByInitialRange(start: String, end: String, pageable: Pageable): Page<Symptom> {
        return symptomRepository.findByNameBetween(start, end, pageable)
    }

    // 전체 증상 개수를 반환하는 함수
    fun countAllSymptoms(): Long {
        return symptomRepository.count()  // JPA의 count() 메서드를 사용하여 전체 개수 반환
    }

    // 특정 초성 범위에 해당하는 증상 개수를 반환하는 메서드
    fun countSymptomsByInitialRange(start: String, end: String): Long {
        return symptomRepository.countByNameRange(start, end)
    }

    // 증상 삭제
    fun deleteSymptom(id: Long): Boolean {
        val symptom = symptomRepository.findById(id).orElse(null)
        return if (symptom != null) {
            symptomRepository.delete(symptom)
            true
        } else {
            false
        }
    }
}
