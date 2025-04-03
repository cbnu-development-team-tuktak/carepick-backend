package com.callrapport.service.disease

// Model (엔티티) 관련 import
import com.callrapport.model.disease.* // Disease, DiseaseRaw, Symptom 등
import com.callrapport.model.common.Specialty // Specialty: 진료과 정보

// Repository (저장소) 관련 import
import com.callrapport.repository.disease.* // 질병 관련 리포지토리
import com.callrapport.repository.common.SpecialtyRepository // 진료과 레포지토리

// 날짜/시간 관련 import
import java.time.LocalDateTime // 생성일 및 수정일 관리를 위한 LocalDateTime

// Spring Data JPA 관련 import 
import org.springframework.data.domain.Page // 페이징된 응답을 위한 객체

// Spring 관련 import
import org.springframework.stereotype.Service // 서비스 클래스 어노테이션
import org.springframework.data.domain.Pageable // 페이지네이션 요청을 처리하는 JPA 객체 (클라이언트가 요청한 페이지 정보 포함)

// ChatGPT 연동 서비스 import
import com.callrapport.component.chatgpt.ChatgptClient // ChatGPT API와 통신하는 클라이언트 컴포넌트

// WebClient 관련 import
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper // JSON 문자열을 객체로 변환하거나 객체를 JSON으로 변환하는 데 사용되는 Jackson ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue // 문자열(JSON)을 Kotlin 객체로 파싱할 수 있는 확장 함수

@Service
class DiseaseService(
    private val diseaseRawRepository: DiseaseRawRepository, // DiseaseRaw 엔티티(DB의 원본 질병 데이터)를 처리하는 JPA 리포지토리
    private val diseaseRepository: DiseaseRepository, // Disease 엔티티(정제된 질병 데이터)를 처리하는 JPA 리포지토리
    private val symptomRepository: SymptomRepository, // Symptom 엔티티(증상 목록)를 처리하는 JPA 리포지토리
    private val diseaseSymptomRepository: DiseaseSymptomRepository, // 질병-증상 간 다대다 관계를 저장하는 JPA 리포지토리
    private val specialtyRepository: SpecialtyRepository, // Specialty 엔티티(진료과 정보)를 처리하는 JPA 리포지토리
    private val diseaseSpecialtyRepository: DiseaseSpecialtyRepository, // 질병-진료과 간 다대다 관계를 저장하는 JPA 리포지토리
    private val diseaseReasoningService: DiseaseReasoningService // ChatGPT 기반 증상 추출 및 진료과 매칭 기능을 제공하는 서비스
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

    // DiseaseRaw 데이터 기반으로 Disease 엔티티 생성
    fun generateCleanDiseasesFromRaw() {
        // // PENDING 상태의 질병 중 상위 10개만 조회 (테스트용)
        // val pendingDiseases = diseaseRawRepository.findByStatus(
        //     DiseaseStatus.PENDING,
        //     Pageable.ofSize(10)
        // )

        // 상태가 PENDING인 질병 원본 데이터만 조회 (아직 처리되지 않은 질병 데이터 대상)
        val pendingDiseases = getDiseasesByStatus(DiseaseStatus.PENDING)

        for (raw in pendingDiseases) {
            try {
                println("🔍 Processing disease: ${raw.name}")
                // ChatGPT를 통해 원본 질병 데이터의 증상 문장에서 증상 키워드 리스트 추출
                val symptoms: List<String> = diseaseReasoningService.extractSymptoms(raw.symptoms).block() ?: emptyList()
                println("Extracted symptoms for '${raw.name}': $symptoms")

                // 증상 추출 결과가 비어 있는 경우
                if (symptoms.isEmpty()) {
                    // 상태를 FAILED로 업데이트
                    updateStatus(raw, DiseaseStatus.FAILED)
                    // 다음 질병 시도
                    continue
                }

                // ChatGPT를 통해 질병명과 증상 리스트를 기반으로 적절한 진료과 리스트 추출
                val suggestedSpecialties = diseaseReasoningService.extractSpecialties(raw.name, symptoms).block() ?: emptyList()
                println("Extracted specialties for '${raw.name}': $suggestedSpecialties")

                // 진료과 추출 결과가 비어 있는 경우 (ChatGPT가 아무 진료과도 추천하지 못한 경우)
                if (suggestedSpecialties.isEmpty()) {
                    // 상태를 FAILED로 변경하고, 다음 질병으로 넘어감
                    updateStatus(raw, DiseaseStatus.FAILED)
                    continue
                }

                // ChatGPT가 추천한 진료과들 중, 실제로 DB에 존재하는 진료과만 필터링
                val validSpecialties = suggestedSpecialties.mapNotNull { specialtyRepository.findByName(it) }

                // 유효한 진료과가 하나도 없을 경우 (추천한 진료과가 기존 진료과 DB에 등록되지 않았을 경우)
                if (validSpecialties.isEmpty()) {
                    // 해당 질병은 저장하지 않고, 처리 상태를 FAILED로 기록
                    println("❌ No registered specialties matched for '${raw.name}' → SKIP")
                    updateStatus(raw, DiseaseStatus.FAILED)
                    continue
                }

                // Disease 생성 및 저장
                val disease = diseaseRepository.save(
                    Disease(
                        name = raw.name, // 질병명
                        bodySystem = raw.bodySystem // 신체계통
                    )
                )

                // Symptom & DiseaseSymptom 저장
                for (symptomName in symptoms) {
                    // 증상명으로 기존 Symptom 엔티티가 있는지 확인
                    val symptom = symptomRepository.findByName(symptomName)
                        // 없다면 새로 생성하여 저장
                        ?: symptomRepository.save(Symptom(name = symptomName))

                    // Disease와 Symptom 사이의 관계(DiseaseSymptom)를 저장
                    diseaseSymptomRepository.save(DiseaseSymptom(disease = disease, symptom = symptom))
                }

                // Specialty & DiseaseSpecialty 저장
                for (specialty in validSpecialties) {
                    diseaseSpecialtyRepository.save(DiseaseSpecialty(disease = disease, specialty = specialty))
                }

                // 처리 성공으로 판단하고 DiseaseRaw 상태를 COMPLETED로 업데이트
                updateStatus(raw, DiseaseStatus.COMPLETED)

            } catch (e: Exception) {
                // 처리 중 예외 발생 시 상태를 FAILED로 설정하여 기록
                updateStatus(raw, DiseaseStatus.FAILED)
            }
        }
    }

    // 상태 업데이트
    private fun updateStatus(raw: DiseaseRaw, status: DiseaseStatus) {
        raw.status = status // 새로운 상태 설정 (예: COMPLETED 또는 FAILED)
        raw.updatedAt = LocalDateTime.now() // 수정 시간을 현재 시각으로 갱신
        diseaseRawRepository.save(raw) // 변경 사항 저장-
    }

    // 모든 질병 정보를 페이지네이션으로 조회
    fun getAllDiseases(
        pageable: Pageable // 페이지 번호, 크기, 정렬 정보를 담은 객체
    ): Page<Disease> {
        return diseaseRepository.findAll(pageable)
    }

    // 전체 질병 개수를 반환하는 함수
    fun countAllDiseases(): Long {
        return diseaseRepository.count()  // JPA의 count() 메서드를 사용하여 전체 개수 반환
    }
}
