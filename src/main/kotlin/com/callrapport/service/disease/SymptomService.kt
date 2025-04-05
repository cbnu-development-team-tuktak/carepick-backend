package com.callrapport.service.disease

// Model (엔티티) 관련 import
import com.callrapport.model.disease.Symptom // 증상 정보를 담는 엔티티

// Repository (저장소) 관련 import
import com.callrapport.repository.disease.SymptomRepository // 증상 데이터를 처리하는 JPA 리포지토리

// Spring 관련 import
import org.springframework.data.domain.Page // 페이징 처리된 결과 객체
import org.springframework.data.domain.Pageable // 페이징 요청 정보를 담는 객체
import org.springframework.stereotype.Service // 해당 클래스가 서비스 계층임을 나타내는 어노테이션

@Service
class SymptomService(
    private val symptomRepository: SymptomRepository // 증상 데이터를 조회하고 관리하는 JPA 리포지토리
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
        return symptomRepository.count()
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
