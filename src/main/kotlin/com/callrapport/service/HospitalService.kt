package com.callrapport.service

// Model (엔티티) 관련 import
import com.callrapport.model.hospital.Hospital // Hospital: 병원 기본 정보 엔티티
import com.callrapport.model.hospital.HospitalDoctor
import com.callrapport.model.hospital.HospitalAdditionalInfo // HospitalAdditionalInfo: 병원 추가 정보 엔티티
import com.callrapport.model.hospital.HospitalSpecialty // HospitalSpecialty: 병원-진료과 연결 엔티티
import com.callrapport.model.common.Specialty // Specialty: 진료과 엔티티
import com.callrapport.model.doctor.Doctor

// Repository (저장소) 관련 import
import com.callrapport.repository.hospital.HospitalRepository // 병원 정보 저장소
import com.callrapport.repository.hospital.HospitalDoctorRepository
import com.callrapport.repository.hospital.HospitalAdditionalInfoRepository // 병원 추가 정보 저장소
import com.callrapport.repository.hospital.HospitalSpecialtyRepository // 병원-진료과 관계 저장소
import com.callrapport.repository.common.SpecialtyRepository // 진료과 정보 저장소

import com.callrapport.repository.doctor.DoctorRepository
// Spring 및 JPA 관련 import
import org.springframework.stereotype.Service // 해당 클래스를 Spring의 서비스 컴포넌트로 등록하는 어노테이션
import org.springframework.transaction.annotation.Transactional // 데이터베이스 트랜잭션을 적용하는 어노테이션
import org.springframework.data.domain.Page // 페이지네이션을 지원하는 JPA의 기본 객체 (검색 결과를 페이지 단위로 관리)
import org.springframework.data.domain.Pageable // 페이지네이션 요청을 처리하는 JPA 객체 (클라이언트가 요청한 페이지 정보 포함)

import org.springframework.transaction.annotation.Propagation

import org.springframework.dao.OptimisticLockingFailureException


@Service
class HospitalService(
    private val hospitalRepository: HospitalRepository, // 병원 저장소
    private val hospitalDoctorRepository: HospitalDoctorRepository,
    private val hospitalAdditionalInfoRepository: HospitalAdditionalInfoRepository, // 병원 추가 정보 저장소
    private val hospitalSpecialtyRepository: HospitalSpecialtyRepository, // 병원-진료과 연결 저장소
    private val specialtyRepository: SpecialtyRepository, // 진료과 저장소
    private val doctorRepository: DoctorRepository // 의사 저장소
) {
    @Transactional
    fun saveHospitalWithDetails(
        id: String,
        name: String,
        phoneNumber: String?,
        homepage: String?,
        address: String,
        operatingHours: String?,
        specialties: List<String>?,
        url: String,
        doctors: List<String>?,
        additionalInfo: HospitalAdditionalInfo?
    ): Hospital {
        // 기존 병원 정보가 존재하는지 확인
        val existingHospital = hospitalRepository.findById(id).orElse(null)

        // 병원 객체 생성
        val hospital = if (existingHospital != null) {
            existingHospital.copy(
                name = name,
                phoneNumber = phoneNumber,
                homepage = homepage,
                address = address,
                operatingHours = operatingHours,
                url = url
            )
        } else {
            Hospital(
                id = id,
                name = name,
                phoneNumber = phoneNumber,
                homepage = homepage,
                address = address,
                operatingHours = operatingHours,
                url = url
            )
        }

        // 병원 저장
        val savedHospital = hospitalRepository.save(hospital)

        // specialties 저장
        if (!specialties.isNullOrEmpty()) {
            val specialtyEntities = specialties.map { specialtyName ->
                val specialty = specialtyRepository.findByName(specialtyName)
                    ?: specialtyRepository.save(Specialty(name = specialtyName))
                HospitalSpecialty(specialty = specialty, hospital = savedHospital)
            }
            hospitalSpecialtyRepository.saveAll(specialtyEntities)
        }

        // 의사 저장
        if (!doctors.isNullOrEmpty()) {
            val doctorEntities = doctorRepository.findAllById(doctors)
            val hospitalDoctors = doctorEntities.map { doctor ->
                HospitalDoctor(hospital = savedHospital, doctor = doctor)
            }
            hospitalDoctorRepository.saveAll(hospitalDoctors)
        }

        // 추가 정보 저장
        if (additionalInfo != null) {
            // hospitalId를 명시적으로 설정하여 새로운 HospitalAdditionalInfo 객체 생성
            val hospitalAdditionalInfo = HospitalAdditionalInfo(
                id = savedHospital.id, // hospitalId를 id로 설정
                hospital = savedHospital,
                open24Hours = additionalInfo.open24Hours,
                emergencyTreatment = additionalInfo.emergencyTreatment,
                maleFemaleDoctorChoice = additionalInfo.maleFemaleDoctorChoice,
                networkHospital = additionalInfo.networkHospital,
                freeCheckup = additionalInfo.freeCheckup,
                nearSubway = additionalInfo.nearSubway,
                openAllYear = additionalInfo.openAllYear,
                openOnSunday = additionalInfo.openOnSunday,
                nightShift = additionalInfo.nightShift,
                collaborativeCare = additionalInfo.collaborativeCare,
                noLunchBreak = additionalInfo.noLunchBreak
            )
            hospitalAdditionalInfoRepository.save(hospitalAdditionalInfo)
        }

        return savedHospital
    }









    fun getHospitalById(hospitalId: String): Hospital? {
        return hospitalRepository.findById(hospitalId).orElse(null)
    }
    // 병원 정보 업데이트
    fun updateHospital(hospital: Hospital): Hospital {
        try {
            return hospitalRepository.save(hospital) // 저장 시 낙관적 잠금(Optimistic Locking) 활성화
        } catch (e: OptimisticLockingFailureException) {
            throw e // 예외가 발생하면 다시 던져서 controller에서 처리할 수 있도록 함
        }
    }
    
    // 모든 병원 정보를 페이지네이션으로 조회
    fun getAllHospitals(
        pageable: Pageable // 페이지네이션 정보를 포함한 객체
    ): Page<Hospital> {
        return hospitalRepository.findAll(pageable)
    }

    // 병원명을 기준으로 병원 검색
    fun searchHospitalsByName(
        keyword: String, // 검색할 병원명
        pageable: Pageable // 페이지네이션 정보를 포함한 객체
    ): Page<Hospital> {
        return hospitalRepository.searchByName(keyword, pageable)
    }

    // 주소를 기준으로 병원 검색
    fun searchHospitalsByAddress(
        keyword: String, // 검색할 주소
        pageable: Pageable  // 페이지네이션 정보를 포함한 객체
    ): Page<Hospital> {
        return hospitalRepository.searchByAddress(keyword, pageable)
    }
}
