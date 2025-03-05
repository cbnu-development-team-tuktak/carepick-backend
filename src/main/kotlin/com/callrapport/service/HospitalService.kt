package com.callrapport.service

// Model (엔티티) 관련 import
import com.callrapport.model.hospital.Hospital // Hospital: 병원 기본 정보 엔티티
import com.callrapport.model.hospital.HospitalDoctor // HospitalDoctor: 병원-의사 관계 엔티티
import com.callrapport.model.hospital.AdditionalInfo // AdditionalInfo: 병원 관련 부가 정보 엔티티
import com.callrapport.model.hospital.HospitalAdditionalInfo // HospitalAdditionalInfo: 병원-병원 관련 부가 정보 연결 엔티티
import com.callrapport.model.hospital.HospitalSpecialty // HospitalSpecialty: 병원-진료과 연결 엔티티
import com.callrapport.model.common.Specialty // Specialty: 진료과 엔티티
import com.callrapport.model.doctor.Doctor // Doctor: 의사 정보 엔티티

// Repository (저장소) 관련 import
import com.callrapport.repository.hospital.HospitalRepository // HospitalRepository: 병원 정보 저장소
import com.callrapport.repository.hospital.AdditionalInfoRepository // AdditionalInfoRepository: 병원 부가 정보 엔티티
import com.callrapport.repository.hospital.HospitalDoctorRepository // HospitalDoctorRepository: 병원-의사 관계 저장소
import com.callrapport.repository.hospital.HospitalAdditionalInfoRepository // HospitalAdditionalInfoRepositor: 병원-병원 부가 정보 관계 저장소
import com.callrapport.repository.hospital.HospitalSpecialtyRepository // HospitalSpecialtyRepository: 병원-진료과 관계 저장소 
import com.callrapport.repository.common.SpecialtyRepository // SpecialtyRepository: 진료과 정보 저장소
import com.callrapport.repository.doctor.DoctorRepository // DoctorRepository: 의사 정보 저장소

// Spring 및 JPA 관련 import
import org.springframework.stereotype.Service // 해당 클래스를 Spring의 서비스 컴포넌트로 등록하는 어노테이션
import org.springframework.transaction.annotation.Transactional // 데이터베이스 트랜잭션을 적용하는 어노테이션
import org.springframework.data.domain.Page // 페이지네이션을 지원하는 JPA의 기본 객체 (검색 결과를 페이지 단위로 관리)
import org.springframework.data.domain.Pageable // 페이지네이션 요청을 처리하는 JPA 객체 (클라이언트가 요청한 페이지 정보 포함)

@Service
class HospitalService(
    private val hospitalRepository: HospitalRepository, // 병원 저장소
    private val additionalInfoRepository: AdditionalInfoRepository, // 병원 부가 정보 저장소
    private val hospitalDoctorRepository: HospitalDoctorRepository, // 병원-의사 관계 저장소
    private val hospitalAdditionalInfoRepository: HospitalAdditionalInfoRepository, // 병원-병원 부가 정보 관계 저장소
    private val hospitalSpecialtyRepository: HospitalSpecialtyRepository, // 병원-진료과 연결 저장소
    private val specialtyRepository: SpecialtyRepository, // 진료과 저장소
    private val doctorRepository: DoctorRepository // 의사 저장소
) {
    @Transactional
    fun saveHospital(
        id: String, // 병원 ID
        name: String, // 병원 이름
        phoneNumber: String?, // 병원 전화번호
        homepage: String?, // 병원 홈페이지
        address: String, // 병원 주소
        operatingHours: String?, // 병원 운영 시간
        specialties: List<String>?, // 병원 진료과 리스트
        url: String?, // 병원 URL
        additionalInfo: Map<String, Any>? // 병원 부가 정보
    ): Hospital {
        // 기존 병원 데이터 확인 (있으면 업데이트, 없으면 새로 생성)
        val existingHospital = hospitalRepository.findById(id).orElse(null)

        // 병원 데이터가 이미 존재하는지 확인
        val hospital = if (existingHospital != null) {
            // 기존 병원 데이터가 존재하면, 해당 데이터를 복사하면서 정보를 업데이트
            existingHospital.copy(
                name = name, // 병원 이름
                phoneNumber = phoneNumber, // 병원 전화번호
                homepage = homepage, // 병원 홈페이지
                address = address, // 병원 주소
                operatingHours = operatingHours, // 병원 운영시간
                url = url // 병원 URL
            )
        } else {
            // 기존 병원 데이터가 없으면 새로운 병원 객체를 생성
            Hospital(
                id = id, // 병원 ID
                name = name, // 병원 이름
                phoneNumber = phoneNumber, // 병원 전화번호
                homepage = homepage, // 병원 홈페이지
                address = address, // 병원 주소
                operatingHours = operatingHours, // 병원 운영시간
                url = url // 병원 URL
            )
        }

        // 병원 정보를 데이터베이스엣 저장
        val savedHospital = hospitalRepository.save(hospital)

        // 진료과가 제공되었을 경우, 병원의 진료과 정보를 저장
        if (!specialties.isNullOrEmpty()) {
            // 진료과 목록을 순회하면서 각 진료과를 처리
            val specialtyEntities = specialties.map { specialtyName ->
                // 진료과 이름으로 진료과를 조회하고, 없으면 새로 저장
                val specialty = specialtyRepository.findByName(specialtyName)
                    ?: specialtyRepository.save(Specialty(name = specialtyName)) // 진료과가 없으면 새로 생성

                // 진료과와 병원을 연결하는 HospitalSpecialty 객체 생성
                HospitalSpecialty(specialty = specialty, hospital = savedHospital)
            }
            // 연결된 진료과들을 데이터베이스에 저장
            hospitalSpecialtyRepository.saveAll(specialtyEntities)
        }

        // 추가 정보가 제공되었으면 AdditionalInfo 저장
        if (additionalInfo != null) {
            // Map을 기반으로 AdditionalInfo 객체 생성
            val additionalInfoEntity = AdditionalInfo(
                open24Hours = additionalInfo["open24Hours"] as? Boolean ?: false, // 24시간 문의 가능 여부
                emergencyTreatment = additionalInfo["emergencyTreatment"] as? Boolean ?: false, // 24시간 응급 환자 진료 가능 여부
                maleFemaleDoctorChoice = additionalInfo["maleFemaleDoctorChoice"] as? Boolean ?: false, // 남여 전문의 선택 가능 여부
                networkHospital = additionalInfo["networkHospital"] as? Boolean ?: false, // 네트워크 병원 여부
                freeCheckup = additionalInfo["freeCheckup"] as? Boolean ?: false, // 무료 검진 제공 여부
                nearSubway = additionalInfo["nearSubway"] as? Boolean ?: false, // 역세권 위치 여부
                openAllYear = additionalInfo["openAllYear"] as? Boolean ?: false, // 연중무휴 진료 여부
                openOnSunday = additionalInfo["openOnSunday"] as? Boolean ?: false, // 일요일 및 공휴일 진료 여부
                nightShift = additionalInfo["nightShift"] as? Boolean ?: false, // 평일 야간 진료 여부
                collaborativeCare = additionalInfo["collaborativeCare"] as? Boolean ?: false, // 협진 시스템 지원 여부
                noLunchBreak = additionalInfo["noLunchBreak"] as? Boolean ?: false // 점심시간 없이 진료 여부
            )

            // 병원 부가 정보를 데이터베이스에 저장
            val savedAdditionalInfo = additionalInfoRepository.save(additionalInfoEntity)

            // 병원 ID와 병원 부가 정보를 연결하여 HospitalAdditionalInfo 엔티티에 저장
            val hospitalAdditionalInfo = HospitalAdditionalInfo(
                id = savedHospital.id,  // 병원 ID를 사용하여 연결
                hospital = savedHospital, // 해당 병원 정보
                additionalInfo = savedAdditionalInfo // 저장된 병원 부가 정보
            )

            // 병원과 병원 부가 정보를 연결하는 정보를 데이터베이스에 저장
            hospitalAdditionalInfoRepository.save(hospitalAdditionalInfo)
        }

        return savedHospital
    }

    // 주어진 병원 ID로 병원 정보를 조회
    fun getHospitalById(hospitalId: String): Hospital? {
        // hospitalRepository에서 병원 ID로 병원 정보를 찾고, 없으면 null 반환
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
