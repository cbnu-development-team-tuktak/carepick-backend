package com.callrapport.service

// Model (엔티티) 관련 import 
import com.callrapport.model.common.Specialty // 진료과 엔티티
import com.callrapport.model.doctor.* // 의사 관련 엔티티들
import com.callrapport.model.hospital.HospitalDoctor // 병원-의사 관계 엔티티

// Repository (저장소) 관련 import
import com.callrapport.repository.common.SpecialtyRepository // 진료과 레포지토리
import com.callrapport.repository.doctor.* // 의사 관련 레포지토리들
import com.callrapport.repository.hospital.HospitalDoctorRepository // 병원-의사 관계 레포지토리
import com.callrapport.repository.hospital.HospitalRepository // 병원 레포지토리

// DTO 관련 import
import com.callrapport.dto.DoctorDetailsResponse // 의사 응답 DTO

// Spring 및 JPA 관련 import
import org.springframework.stereotype.Service // 서비스 클래스 어노테이션
import org.springframework.transaction.annotation.Transactional // 트랜잭션 처리 어노테이션
import org.springframework.data.domain.Page // 페이지 결과 객체
import org.springframework.data.domain.Pageable // 페이지 요청 객체

@Service
class DoctorService(
    // Repository: 의사 관련
    private val doctorRepository: DoctorRepository, // 의사 정보를 저장/조회하는 레포지토리
    private val doctorEducationLicenseRepository: DoctorEducationLicenseRepository, // 의사-자격면허 관계 레포지토리
    private val doctorSpecialtyRepository: DoctorSpecialtyRepository, // 의사-진료과 관계 레포지토리
    private val doctorCareerRepository: DoctorCareerRepository, // 의사-경력 관계 레포지토리

    // Repository: 병원 및 공통 관련
    private val hospitalRepository: HospitalRepository, // 병원 정보를 저장/조회하는 레포지토리
    private val hospitalDoctorRepository: HospitalDoctorRepository, // 병원-의사 관계 레포지토리
    private val specialtyRepository: SpecialtyRepository, // 진료과 정보를 저장/조회하는 레포지토리
    private val educationLicenseRepository: EducationLicenseRepository, // 자격면허 정보를 저장/조회하는 레포지토리
    private val careerRepository: CareerRepository, // 경력 정보를 저장/조회하는 레포지토리
    
) {
    // 의사 정보를 생성하거나 기존 정보 업데이트
    private fun createOrUpdateDoctor(
        id: String, // 의사 ID
        name: String, // 의사 이름
        profileImage: String? // 의사 프로필 이미지
    ): Doctor {
        // 기존 의사 정보가 이미 존재하는지 확인
        val existingDoctor = doctorRepository.findById(id).orElse(null)

        val doctor = if (existingDoctor != null) { // 기존 의사 정보가 이미 존재하는 경우
            // 이름과 프로필 이미지만 변경하고 나머지 필드는 기존 값 유지
            existingDoctor.copy( 
                name = name, // 새로 전달된 이름으로 업데이트
                profileImage = profileImage // 새로 전달된 프로필 이미지로 업데이트
            )
        } else { // 기존 의사 정보가 존재하지 않는 경우
            // 새로운 Doctor 객체 생성
            Doctor(
                id = id, // 새로운 의사 ID 설정
                name = name, // 의사 이름 설정
                profileImage = profileImage // 프로필 이미지 설정
            )
        }

        // 의사 정보 저장 또는 업데이트
        return doctorRepository.save(doctor)
    }

    // 의사와 진료과 간의 관계(DoctorSpecialty)를 저장
    private fun saveDoctorSpecialties(
        savedDoctor: Doctor, // 저장된 Doctor 객체
        specialtyNames: List<String>? // 진료과 이름 리스트 (예: ["내과", "정형외과"])
    ) {
        // 의사와 진료과(N:M) 관계 저장
        if (!specialtyNames.isNullOrEmpty()) { // 진료과 리스트가 null이 아니고 비어있지 않은 경우에만 처리
            val doctorSpecialties = specialtyNames.mapNotNull { specialtyName ->
                // 진료과 이름을 기준으로 Specialty 엔티티 조회
                val specialty = specialtyRepository.findByName(specialtyName)
                    ?: return@mapNotNull null // 해당 진료과가 DB에 존재하지 않으면 매핑하지 않고 건너뜀
                
                // 의사-진료과 관계 객체 생성 (DoctorSpecialty)
                // 이미 존재하는 Specialty 엔티티를 참조하여 관계 설정
                DoctorSpecialty(
                    doctor = savedDoctor, // 현재 저장 중인 의사 엔티티
                    specialty = specialty // 조회된 Specialty 엔티티
                ) 
            }
            // 생성된 의사-진료과 관계 리스트를 DB에 일괄 저장
            doctorSpecialtyRepository.saveAll(doctorSpecialties)
        }
    }

    // 의사와 경력(N:M) 관계 저장
    private fun saveDoctorCareers(savedDoctor: Doctor, careerNames: List<String>?) {
        if (!careerNames.isNullOrEmpty()) { // 경력 리스트가 null이 아니고 비어있지 않은 경우에만 처리
            val doctorCareers = careerNames.mapNotNull { careerName ->
                // 경력 이름을 기준으로 Career 엔티티 조회
                // 존재하지 않는 경우 새로 생성하여 저장
                val career = careerRepository.findByName(careerName)
                    ?: careerRepository.save(Career(name = careerName))

                // 해당 의사-경력 조합이 이미 DB에 존재하는지 확인
                val exists = doctorCareerRepository.existsByDoctorIdAndCareerId(
                    savedDoctor.id, 
                    career.id!!
                )
                if (!exists) {
                    // 중복되지 않은 경우에만 관계 객체 생성
                    DoctorCareer(
                        doctor = savedDoctor, // 현재 저장 중인 의사
                        career = career // 조회 또는 생성된 경력
                    )  
                } else {
                    // 이미 존재하는 경우 null을 반환하여 저장하지 않음
                    null  
                }
            }.filterNotNull() // null 값을 제거하여 실제 저장할 객체만 필터링
            
            // 생성된 의사-경력 관계 리스트를 DB에 일괄 저장
            doctorCareerRepository.saveAll(doctorCareers)  
        }
    }

    // 의사와 자격면허(N:M) 관계 저장
    private fun saveDoctorEducationLicenses(savedDoctor: Doctor, educationLicenseNames: List<String>?) {    
        if (!educationLicenseNames.isNullOrEmpty()) { // 자격면허 리스트가 null이 아니고 비어있지 않은 경우에만 처리
            val doctorLicenses = educationLicenseNames
                .distinct() // 중복된 자격면허 이름 제거
                .mapNotNull { licenseName -> 
                    // 자격면허 이름으로 EducationLicense 엔티티 조회
                    // 존재하지 않으면 새로 생성하여 저장
                val license = educationLicenseRepository.findByName(licenseName)
                    ?: educationLicenseRepository.save(EducationLicense(name = licenseName))

                // 해당 의사-자격면허 조합이 이미 DB에 존재하는지 확인
                val exists = doctorEducationLicenseRepository
                    .existsByDoctorIdAndEducationLicenseId(savedDoctor.id, license.id!!)

                if (!exists) {
                    // 중복되지 않은 경우에만 관계 객체 생성
                    DoctorEducationLicense(
                        doctor = savedDoctor, // 현재 저장 중인 의사
                        educationLicense = license // 조회 또는 생성된 자격면허
                    )
                } else {
                    // 이미 존재하는 경우 null 반환하여 저장하지 않음
                    null 
                }
            }.filterNotNull() // null 값을 제거하여 실제 저장할 객체만 필터링

            // 생성된 의사-자격면허 관계 리스트를 DB에 일괄 저장
            doctorEducationLicenseRepository.saveAll(doctorLicenses)
        }
    }

    // 병원과 의사(N:M) 연결
    private fun linkDoctorToHospital(savedDoctor: Doctor, hospitalId: String?) {
        if (hospitalId != null) { // 병원 ID가 null이 아닌 경우에만 처리 
            // 병원 ID를 기준으로 Hospital 엔티티 조회
            val hospital = hospitalRepository.findById(hospitalId).orElse(null)

            if (hospital != null) {
                // 병원과 의사 관계를 나타내는 HospitalDoctor 객체 생성
                val hospitalDoctor = HospitalDoctor(
                    hospital = hospital, // 조회된 병원 객체
                    doctor = savedDoctor // 저장된 의사 객체
                )
                // 병원-의사 관계 저장
                hospitalDoctorRepository.save(hospitalDoctor)
            }
        }
    }
    
    @Transactional
    fun saveDoctorWithDetails(
        id: String, // 의사 ID
        name: String, // 의사 이름
        profileImage: String?, // 의사 프로필 이미지
        educationLicenses: List<String>?, // 자격면허 ID 리스트
        hospitalId: String?, // 의사가 소속된 병원 ID
        specialtyNames: List<String>?, // 진료과 이름 리스트 (예: ["내과", "정형외과"])
        careerNames: List<String>?, // 의사 경력 이름 리스트 (예: ["서울대병원 수련", "삼성서울병원 교수"])
        educationLicenseNames: List<String>? // 의사 자격면허 이름 리스트 (예: ["전문의", "의사면허"])
    ): Doctor {    
        // 의사 정보 생성 또는 기존 정보 업데이트 후 저장
        val savedDoctor = createOrUpdateDoctor(id, name, profileImage)

        // 의사와 진료과 간의 관계 저장 (DoctorSpecialty)
        saveDoctorSpecialties(savedDoctor, specialtyNames)

        // 의사와 경력 간의 관계 저장 (DoctorCareer)
        // 경력이 없으면 새로 생성하고, 중복은 방지함
        saveDoctorCareers(savedDoctor, careerNames)

        // 의사와 자격면허 간의 관계 저장 (DoctorEducationLicense)
        // 자격면허가 없으면 생성하고, 중복은 방지함
        saveDoctorEducationLicenses(savedDoctor, educationLicenseNames)

        // 병원-의사 관계 저장 (HospitalDoctor)
        // 병원 ID가 유효한 경우에만 연결
        linkDoctorToHospital(savedDoctor, hospitalId)

        // 최종 저장된 의사 객체 반환
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

    // 의사 ID를 기준으로 의사 검색
    fun getDoctorById(id: String): Doctor? {
        return doctorRepository.findById(id).orElse(null)
    }

    // 의사 ID를 기준으로 첫 번째 병원-의사 관계 조회
    fun getFirstHospitalDoctorByDoctorId(doctorId: String): HospitalDoctor? {
        return hospitalDoctorRepository.findFirstByDoctorId(doctorId)
    }
}
