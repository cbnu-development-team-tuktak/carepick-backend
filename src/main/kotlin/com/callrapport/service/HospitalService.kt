package com.callrapport.service

// Model (엔티티) 관련 import
import com.callrapport.model.hospital.Hospital // Hospital: 병원 기본 정보 엔티티
import com.callrapport.model.hospital.HospitalAdditionalInfo // HospitalAdditionalInfo: 병원 추가 정보 엔티티
import com.callrapport.model.hospital.HospitalSpecialty // HospitalSpecialty: 병원-진료과 연결 엔티티
import com.callrapport.model.common.Specialty // Specialty: 진료과 엔티티

// Repository (저장소) 관련 import
import com.callrapport.repository.hospital.HospitalRepository // 병원 정보 저장소
import com.callrapport.repository.hospital.HospitalAdditionalInfoRepository // 병원 추가 정보 저장소
import com.callrapport.repository.hospital.HospitalSpecialtyRepository // 병원-진료과 관계 저장소
import com.callrapport.repository.common.SpecialtyRepository // 진료과 정보 저장소

// Spring 및 JPA 관련 import
import org.springframework.stereotype.Service // 해당 클래스를 Spring의 서비스 컴포넌트로 등록하는 어노테이션
import org.springframework.transaction.annotation.Transactional // 데이터베이스 트랜잭션을 적용하는 어노테이션
import org.springframework.data.domain.Page // 페이지네이션을 지원하는 JPA의 기본 객체 (검색 결과를 페이지 단위로 관리)
import org.springframework.data.domain.Pageable // 페이지네이션 요청을 처리하는 JPA 객체 (클라이언트가 요청한 페이지 정보 포함)

@Service
class HospitalService(
    private val hospitalRepository: HospitalRepository, // 병원 저장소
    private val hospitalAdditionalInfoRepository: HospitalAdditionalInfoRepository, // 병원 추가 정보 저장소
    private val hospitalSpecialtyRepository: HospitalSpecialtyRepository, // 병원-진료과 연결 저장소
    private val specialtyRepository: SpecialtyRepository // 진료과 저장소 
) {
    @Transactional
    fun saveHospital(
        id: String,
        name: String,
        phoneNumber: String?,
        homepage: String?,
        address: String,
        operatingHours: String?,
        specialties: List<String>?,
        url: String?
    ): Hospital {
        // 기존 병원 데이터 확인 (있으면 업데이트, 없으면 새로 생성)
        val existingHospital = hospitalRepository.findById(id).orElse(null)

        val hospital = if (existingHospital != null) {
            existingHospital.copy(
                name = name,
                phoneNumber = phoneNumber,
                homepage = homepage,
                address = address,
                operatingHours = operatingHours,
            )
        } else {
            Hospital(
                id = id,
                name = name,
                phoneNumber = phoneNumber,
                homepage = homepage,
                address = address,
                operatingHours = operatingHours
            )
        }
        // 병원 정보 저장
        val savedHospital = hospitalRepository.save(hospital)

        // 병원 진료과 연결 정보 저장 (기존 데이터 삭제 후 추가)
        if (specialties != null) {
            hospitalSpecialtyRepository.deleteAll(hospitalSpecialtyRepository.findByHospital(savedHospital))

            specialties.forEach { specialtyName ->
                val specialty = specialtyRepository.findByName(specialtyName)
                    ?: specialtyRepository.save(Specialty(name = specialtyName))
                
                    val hospitalSpecialty = HospitalSpecialty(
                        hospital = savedHospital,
                        specialty = specialty
                    )

                    hospitalSpecialtyRepository.save(hospitalSpecialty)
            }
        }

        return savedHospital
    }
}

