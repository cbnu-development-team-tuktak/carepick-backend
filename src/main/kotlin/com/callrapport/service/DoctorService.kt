package com.callrapport.service

// Model (엔티티) 관련 import 
import com.callrapport.model.doctor.Doctor // Doctor 엔티티: 의사 정보를 저장하는 엔티티 (의사 ID, 이름, 프로필 이미지, 진료과 등)
import com.callrapport.model.doctor.DoctorEducationLicense // DoctorEducationLicense 엔티티: 의사의 학력 및 자격면허 정보를 저장하는 엔티티
import com.callrapport.model.common.Specialty // Specialty 엔티티: 의사의 진료과 정보를 저장하는 엔티티

// Repository (저장소) 관련 import
import com.callrapport.repository.doctor.DoctorRepository // 의사 정보를 데이터베이스에서 조회/저장하는 인터페이스
import com.callrapport.repository.doctor.DoctorEducationLicenseRepository // 의사의 자격면허 정보를 관리하는 저장소
import com.callrapport.repository.common.SpecialtyRepository // 진료과 정보를 데이터베이스에서 조회/저장하는 인터페이스

// DTO 관련 import
import com.callrapport.dto.DoctorDetailsResponse // API 응답에서 Doctor 정보를 가공하여 반환하는 DTO

// Spring 및 JPA 관련 import
import org.springframework.stereotype.Service // 해당 클래스를 Spring의 서비스 컴포넌트로 등록하는 어노테이션
import org.springframework.transaction.annotation.Transactional // 데이터베이스 트랜잭션을 적용하는 어노테이션
import org.springframework.data.domain.Page // 페이지네이션을 지원하는 JPA의 기본 객체 (검색 결과를 페이지 단위로 관리)
import org.springframework.data.domain.Pageable // 페이지네이션 요청을 처리하는 JPA 객체 (클라이언트가 요청한 페이지 정보 포함)

@Service
class DoctorService(
    private val doctorRepository: DoctorRepository, // 의사 저장소 (JPA Repository)
    private val doctorEducationRepository: DoctorEducationLicenseRepository, // 자격면허 저장소 (JPA Repository)
    private val specialtyRepository: SpecialtyRepository // 진료과 저장소 (JPA Repository)
) {
    @Transactional
    fun saveDoctorWithDetails(
        id: String, // 의사 ID
        name: String, // 의사 이름
        profileImage: String?, // 프로필 이미지 URL
        educationLicenses: List<String>?, // 자격면허 리스트
        hospitalId: String?, // 병원 ID
        specialtyName: String? // 진료과 이름
    ): Doctor {    
        // 진료과를 조회하거나 생성
        val specialty = specialtyName?.let { name ->
            specialtyRepository.findByName(name) ?: specialtyRepository.save(Specialty(name = name))
        }

        // Doctor 객체 생성
        val doctor = Doctor(
            id = id, 
            name = name, 
            profileImage = profileImage, 
            hospitalId = hospitalId,
            specialty = specialty
        )

        /// 의사 정보 저장
        val savedDoctor = doctorRepository.save(doctor) 

        // 자격면허 리스트가 비어있지 않을 경우, 여러 개의 자격면허를 저장
        educationLicenses?.forEach { license ->
            val doctorEducation = DoctorEducationLicense(
                educationLicense = license,
                doctor = savedDoctor
            )
            doctorEducationRepository.save(doctorEducation) // 자격면허 저장
        }

        return savedDoctor
    }

    // 모든 의사 정보를 페이지네이션으로 조회
    fun getAllDoctors(
        pageable: Pageable // 페이지네이션 정보를 포함한 객체
    ): Page<Doctor> {
        return doctorRepository.findAll(pageable)
    }

    // 이름을 기준으로 의사 검색
    fun searchDoctorsByName(
        keyword: String, // 검색할 의사 이름
        pageable: Pageable // 페이지네이션 정보를 포함한 객체
    ): Page<Doctor> {
        return doctorRepository.searchByName(keyword, pageable)
    }
}
