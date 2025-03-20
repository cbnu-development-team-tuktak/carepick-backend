package com.callrapport.service

// Model (엔티티) 관련 import 
import com.callrapport.model.doctor.Doctor
import com.callrapport.model.doctor.DoctorEducationLicense
import com.callrapport.model.doctor.DoctorSpecialty // ✅ 추가
import com.callrapport.model.hospital.HospitalDoctor // ✅ 추가
import com.callrapport.model.common.Specialty
import com.callrapport.model.doctor.EducationLicense
import com.callrapport.model.doctor.Career
import com.callrapport.model.doctor.DoctorCareer

// Repository (저장소) 관련 import
import com.callrapport.repository.doctor.DoctorRepository
import com.callrapport.repository.doctor.DoctorEducationLicenseRepository
import com.callrapport.repository.doctor.DoctorSpecialtyRepository // ✅ 추가
import com.callrapport.repository.hospital.HospitalDoctorRepository // ✅ 추가
import com.callrapport.repository.hospital.HospitalRepository // ✅ 추가
import com.callrapport.repository.common.SpecialtyRepository
import com.callrapport.repository.doctor.EducationLicenseRepository
import com.callrapport.repository.doctor.CareerRepository
import com.callrapport.repository.doctor.DoctorCareerRepository

// DTO 관련 import
import com.callrapport.dto.DoctorDetailsResponse

// Spring 및 JPA 관련 import
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Service
class DoctorService(
    private val doctorRepository: DoctorRepository,
    private val doctorEducationLicenseRepository: DoctorEducationLicenseRepository,
    private val specialtyRepository: SpecialtyRepository,
    private val educationLicenseRepository: EducationLicenseRepository,
    private val doctorSpecialtyRepository: DoctorSpecialtyRepository, // ✅ 추가
    private val hospitalRepository: HospitalRepository, // ✅ 추가
    private val hospitalDoctorRepository: HospitalDoctorRepository, // ✅ 추가
    private val careerRepository: CareerRepository,
    private val doctorCareerRepository: DoctorCareerRepository
) {
    @Transactional
    fun saveDoctorWithDetails(
        id: String,
        name: String,
        profileImage: String?,
        educationLicenses: List<String>?,
        hospitalId: String?,
        specialtyNames: List<String>?, // ✅ 여러 개의 진료과 가능하도록 수정
        careerNames: List<String>?,
        educationLicenseNames: List<String>?
    ): Doctor {    
        // 기존 의사 정보 확인 (있으면 업데이트, 없으면 새로 생성)
        val existingDoctor = doctorRepository.findById(id).orElse(null)

        // ✅ 의사 객체 생성 또는 업데이트 (specialty 제거)
        val doctor = if (existingDoctor != null) {
            existingDoctor.copy(
                name = name,
                profileImage = profileImage
            )
        } else {
            Doctor(
                id = id,
                name = name,
                profileImage = profileImage
            )
        }

        // ✅ 의사 정보 저장
        val savedDoctor = doctorRepository.save(doctor)

        // 
        if (!specialtyNames.isNullOrEmpty()) { // ✅ 널 체크 추가
            val doctorSpecialties = specialtyNames.mapNotNull { specialtyName ->
                val specialty = specialtyRepository.findByName(specialtyName)
                    ?: return@mapNotNull null // ❌ 존재하지 않으면 저장하지 않음
        
                DoctorSpecialty(doctor = savedDoctor, specialty = specialty) // ✅ 기존 Specialty ID 사용
            }
            doctorSpecialtyRepository.saveAll(doctorSpecialties)
        }
        


        // ✅ 경력 저장 (N:M 관계)
        if (!careerNames.isNullOrEmpty()) {
            val doctorCareers = careerNames.map { careerName ->
                val career = careerRepository.findByName(careerName)
                    ?: careerRepository.save(Career(name = careerName))
                DoctorCareer(doctor = savedDoctor, career = career)
            }
            doctorCareerRepository.saveAll(doctorCareers)
        }

        // ✅ 자격면허 저장 (N:M 관계)
        if (!educationLicenseNames.isNullOrEmpty()) {
            val doctorEducations = educationLicenseNames.map { licenseName ->
                val educationLicense = educationLicenseRepository.findByName(licenseName)
                    ?: educationLicenseRepository.save(EducationLicense(name = licenseName))
                DoctorEducationLicense(doctor = savedDoctor, educationLicense = educationLicense)
            }
            doctorEducationLicenseRepository.saveAll(doctorEducations)
        }


        // ✅ 병원과 의사 연결 (N:M 관계)
        if (hospitalId != null) {
            val hospital = hospitalRepository.findById(hospitalId).orElse(null)
            if (hospital != null) {
                val hospitalDoctor = HospitalDoctor(
                    hospital = hospital,
                    doctor = savedDoctor
                )
                hospitalDoctorRepository.save(hospitalDoctor)
            }
        }

        return savedDoctor
    }


    // 모든 의사 정보를 페이지네이션으로 조회
    fun getAllDoctors(pageable: Pageable): Page<Doctor> {
        return doctorRepository.findAll(pageable)
    }

    // 이름을 기준으로 의사 검색
    fun searchDoctorsByName(keyword: String, pageable: Pageable): Page<Doctor> {
        return doctorRepository.searchByName(keyword, pageable)
    }
}
