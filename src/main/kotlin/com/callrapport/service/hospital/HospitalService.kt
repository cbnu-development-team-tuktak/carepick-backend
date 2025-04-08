package com.callrapport.service

// Model (엔티티) 관련 import
import com.callrapport.model.hospital.* // 병원 관련 엔티티들
import com.callrapport.model.common.* // 공통 엔티티
import com.callrapport.model.doctor.* // 의사 관련 엔티티

// Repository (저장소) 관련 import
import com.callrapport.repository.hospital.* // 병원 관련 저장소
import com.callrapport.repository.common.* // 공통 저장소
import com.callrapport.repository.doctor.* // 의사 관련 저장소
import com.callrapport.repository.user.* // 사용자 관련 저장소

// Spring 및 JPA 관련 import
import org.springframework.stereotype.Service // 해당 클래스를 Spring의 서비스 컴포넌트로 등록하는 어노테이션
import org.springframework.transaction.annotation.Transactional // 데이터베이스 트랜잭션을 적용하는 어노테이션
import org.springframework.data.domain.Page // 페이지네이션을 지원하는 JPA의 기본 객체 (검색 결과를 페이지 단위로 관리)
import org.springframework.data.domain.Pageable // 페이지네이션 요청을 처리하는 JPA 객체 (클라이언트가 요청한 페이지 정보 포함)
import org.springframework.dao.OptimisticLockingFailureException // 낙관적 락(Optimistic Lock) 예외 처리

// REST API 요청 관련 import (카카오맵 API 활용)
import org.springframework.web.client.RestTemplate // REST API 요청을 위한 Spring ResteTemplate
import org.springframework.web.util.UriComponentsBuilder // URL 빌더
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpHeaders // HTTP 요청 헤더 관리
import org.springframework.http.HttpEntity // HTTP 요청 엔티티 (Header + Body 포함)
import org.springframework.http.HttpMethod // HTTP 요청 메서드 (GET, POST 등)

// JSON 데이터 처리 관련 import
import com.fasterxml.jackson.databind.JsonNode // JSON 데이터를 트리 구조로 표현하는 Jackson 클래스
import com.fasterxml.jackson.databind.ObjectMapper // JSON 파싱 및 객체 매핑을 위한 Jackson 핵심 클래스

// 공간 데이터(geo) 관련 import (병원 위치 좌표 관리)
import org.locationtech.jts.geom.Coordinate // 좌표 데이터 타입
import org.locationtech.jts.geom.GeometryFactory // 공간 데이터 객체 생성
import org.locationtech.jts.geom.Point // 병원 위치를 저장하는 Point 타입
import org.locationtech.jts.geom.PrecisionModel // 좌표 정밀도 설정

// Component (컴포넌트) 관련 import
import com.callrapport.component.map.Geolocation // 위치 좌표 변환 및 지리 정보 처리를 담당하는 컴포넌트

@Service
class HospitalService(
    // 의사 관련 레포지토리
    private val doctorRepository: DoctorRepository, // 의사 저장소
    private val doctorSpecialtyRepository: DoctorSpecialtyRepository, // 의사-진료과 연결 저장소
    private val doctorCareerRepository: DoctorCareerRepository, // 의사-경력 연결 저장소
    private val doctorEducationLicenseRepository: DoctorEducationLicenseRepository, // 의사-자격면허 연결 저장소
    private val educationLicenseRepository: EducationLicenseRepository, // 자격면허 저장소
    private val careerRepository: CareerRepository, // 경력 저장소

    // 병원 관련 레포지토리
    private val hospitalRepository: HospitalRepository, // 병원 저장소
    private val hospitalDoctorRepository: HospitalDoctorRepository, // 병원-의사 관계 저장소
    private val hospitalSpecialtyRepository: HospitalSpecialtyRepository, // 병원-진료과 관계 저장소
    private val hospitalAdditionalInfoRepository: HospitalAdditionalInfoRepository, // 병원-부가정보 연결 저장소
    private val hospitalImageRepository: HospitalImageRepository, // 병원-이미지 연결 저장소
    private val additionalInfoRepository: AdditionalInfoRepository, // 병원 부가 정보 저장소

    // 공통 관련 레포지토리
    private val specialtyRepository: SpecialtyRepository, // 진료과 저장소
    private val imageRepository: ImageRepository, // 이미지 저장소

    // 사용자 관련 레포지토리
    private val userFavoriteHospitalRepository: UserFavoriteHospitalRepository, // 즐겨찾는 병원 저장소  

    // 좌표 변환을 위한 컴포넌트
    private val geolocation: Geolocation
) {
    // 병원과 연관된 이미지들을 저장하고, 병원-이미지 관계(HospitalImage)를 설정한다. 
    @Transactional
    fun saveHospitalImages(
        hospital: Hospital, // 이미지와 연결할 병원 객체
        images: List<Image> // 저장할 이미지 리스트
    ) {
        // 이미지 URL을 기준으로 기존 이미지가 DB에 있는지 확인
        images.forEach { image ->
            // 이미지 URL을 기준으로 기존 이미지에 DB가 있는지 확인
            val existingImage = imageRepository.findByUrl(image.url)
            
            // 기존 이미지가 없으면 새로 저장하고, 있으면 해당 객체 사용
            val savedImage = existingImage ?: imageRepository.save(image)

            // 병원-이미지 관계가 이미 존재하는지 확인
            val exists = hospitalImageRepository.existsByHospitalIdAndImageId(
                hospital.id, 
            savedImage.id!!)
            
            if (!exists) {
                // 병원-이미지 관계가 존재하지 않으면 새로 저장
                val hospitalImage = HospitalImage(
                    hospital = hospital, // 연결할 병원
                    image = savedImage // 연결할 이미지
                )
                hospitalImageRepository.save(hospitalImage)
            } else {
                // 이미 병원-이미지 관계가 존재하는 경우 로그 출력
                println("⚠️ Hospital-Image relation already exists: ${hospital.id} - ${savedImage.id}")
            }
        }
    }

    private fun createOrUpdateHospital(
        id: String, // 병원 ID 
        name: String, // 병원 이름
        phoneNumber: String?, // 전화번호
        homepage: String?, // 홈페이지 URL 
        address: String?, // 병원 주소
        operatingHours: String?, // 병원 운영 시간
        url: String? // 병원 상세 정보 URL
    ): Hospital {
        // 기존 병원 데이터 확인 (있으면 업데이트, 없으면 새로 생성)
        val existingHospital = hospitalRepository.findById(id).orElse(null)

        // 병원 객체 생성 또는 업데이트
        val hospital = if (existingHospital != null) {
            // 기존 병원 정보가 존재하는 경우, 값 일부만 수정하여 새 객체로 복사
            existingHospital.copy(
                name = name, // 병원명 업데이트
                phoneNumber = phoneNumber, // 전화번호 업데이트
                homepage = homepage, // 홈페이지 주소 업데이트
                address = address, // 주소 업데이트
                operatingHours = operatingHours, // 운영 시간 업데이트
                url = url // 상세 정보 URL 업데이트
            )
        } else {
            // 병원 정보가 존재하지 않는 경우, 새 병원 객체 생성
            Hospital(
                id = id, // 병원 ID
                name = name, // 병원명
                phoneNumber = phoneNumber, // 전화번호
                homepage = homepage, // 홈페이지 주소
                address = address, // 주소
                operatingHours = operatingHours, // 운영 시간
                url = url // 상세 정보 URL
            )
        }

        // 병원 정보 저장 (신규 또는 수정된 병원 정보 DB에 반영)
        return hospitalRepository.save(hospital)
    }

    // 병원과 진료과의 관계(HospitalSpecialty)를 저장
    private fun saveHospitalSpecialties(savedHospital: Hospital, specialties: List<String>?) {
        // 병원의 진료과 정보 저장 (중복 방지)
        if (!specialties.isNullOrEmpty()) {
            // 전달받은 진료과 이름 리스트에서 중복을 제거하고 각 이름에 대해 처리
            val specialtyEntities = specialties.distinct().mapNotNull { specialtyName ->
                // 진료과 이름으로 Specialty 엔티티 조회 (DB에 존재하는 경우만 처리)
                val specialty = specialtyRepository.findByName(specialtyName)
                    ?: return@mapNotNull null // 존재하지 않는 진료과는 무시하고 다음으로 넘어간다

                // 병원-진료과 관계가 이미 존재하는지 확인 (중복 저장 방지)
                val exists = hospitalSpecialtyRepository.existsByHospitalIdAndSpecialtyId(savedHospital.id, specialty.id)
                if (!exists) {
                    // 중복이 아닌 경우 새로운 관계 객체 생성
                    HospitalSpecialty(
                        specialty = specialty, // 조회된 Specialty 객체
                        hospital = savedHospital // 저장된 병원 객체
                    )
                } else {
                    null // 이미 존재하면 null 반환하여 이후에 제거한다
                }
            }.filterNotNull() // null 제거하여 실제로 저장할 객체만 리스트로 유지
            
            // 생성된 병원-진료과 관계 리스트를 DB에 일괄 저장
            hospitalSpecialtyRepository.saveAll(specialtyEntities) 
        }
    }

    private fun createOrUpdateDoctor(
        doctorData: Map<String, String?>
    ): Doctor? {
        // 의사 ID 추출 (null이면 해당 데이터는 건너뜀)
        val doctorId = doctorData["id"] as? String ?: throw IllegalArgumentException("의사 ID가 누락되었습니다")
            
        // 의사 이름 추출 (null이면 해당 데이터는 건너뜀)
        val doctorName = doctorData["name"] as? String ?: throw IllegalArgumentException("의사 이름이 누락되었습니다")
        
        // 프로필 이미지 URL (선택 값)
        val profileImage = doctorData["profileImage"]

        // 의사 정보 저장 (기존 정보가 있으면 업데이트, 없으면 새로 저장)
        val existingDoctor = doctorRepository.findById(doctorId).orElse(null) // 기존 의사 정보 조회
        // 기존 의사가 존재할 경우 이름과 프로필 이미지만 업데이트
        val doctor = if (existingDoctor != null) {
            existingDoctor.copy(
                name = doctorName, // 새로 받은 이름으로 덮어쓰기
                profileImage = profileImage ?: existingDoctor.profileImage // 새 이미지가 있으면 갱신, 없으면 기존 유지
            )
        } else {
            // 기존 의사가 존재하지 않는 경우 새 Doctor 객체 생성
            Doctor(
                id = doctorId, // 의사 ID 설정
                name = doctorName, // 의사 이름 설정
                profileImage = profileImage // 프로필 이미지 설정
            )
        }

        // 의사 정보를 doctorRepository에 저장
        return doctorRepository.save(doctor)
    }
    
    // 의사와 진료과의 관계(DoctorSpecialty)를 저장
    private fun saveDoctorSpecialties(
        savedDoctor: Doctor, // 저장된 의사 객체
        specialtyNames: List<String> // 진료과 이름 리스트
    ) {
        if (specialtyNames.isNotEmpty()) { // 진료과 이름 리스트가 비어있지 않은 경우에만 처리
            val doctorSpecialties = specialtyNames
                .distinct() // 중복된 진료과 이름 제거
                .mapNotNull { specialtyName -> 
                // 진료과 이름을 기준으로 Specialty 엔티티 조회
                val specialty = specialtyRepository.findByName(specialtyName)
                    ?: return@mapNotNull null // 존재하지 않으면 해당 항목은 건너뛴다

                //  해당 의사-진료과 관계가 이미 존재하는지 확인
                val exists = doctorSpecialtyRepository.existsByDoctorIdAndSpecialtyId(
                    savedDoctor.id, 
                    specialty.id
                )

                if (!exists) {
                    // 중복되지 않은 경우에만 DoctorSpecialty 객체 생성
                    DoctorSpecialty(
                        doctor = savedDoctor, // 저장된 의사 객체
                        specialty = specialty // 조회된 진료과 객체
                    )
                } else {
                    null // 이미 존재하는 관계는 저장하지 않음
                }
            }.filterNotNull() // null 제거
            // 생성된 의사-진료과 관계 리스트를 DB에 일괄 저장
            doctorSpecialtyRepository.saveAll(doctorSpecialties) 
        }          
    }
    
    // 의사와 경력의 관계(DoctorCareer)를 저장
    private fun saveDoctorCareers(
        savedDoctor: Doctor,
        careerNames: List<String>
    ) {
        if (!careerNames.isNullOrEmpty()) { // 경력 이름 리스트가 null이 아니고 비어있지 않은 경우
            val doctorCareers = careerNames
                .distinct() // 중복된 경력 이름 제거
                .mapNotNull { careerName ->
                // 경력 이름을 기준으로 Career 엔티티 조회
                // 존재하지 않으면 새로 생성하여 저장
                val career = careerRepository.findByName(careerName) 
                    ?: careerRepository.save(Career(name = careerName))

                // 해당 의사-경력 관계가 이미 존재하는지 확인
                val exists = doctorCareerRepository.existsByDoctorIdAndCareerId(
                    savedDoctor.id, 
                    career.id!!
                )

                if (!exists) {
                    // 중복되지 않은 경우에만 DoctorCareer 객체 생성
                    DoctorCareer(
                        doctor = savedDoctor, // 저장된 의사 객체
                        career = career // 조회 또는 생성된 경력 객체
                    )
                } else {
                    null // 이미 존재하는 관계는 저장하지 않음
                }
            }.filterNotNull() // Null 값 제거

            // 생성된 의사-경력 관계 리스트를 DB에 일괄 저장
            doctorCareerRepository.saveAll(doctorCareers)
        }
    }

    // 의사와 자격면허의 관계(DoctorEducationLicense)를 저장
    private fun saveDoctorEducationLicenses(
        savedDoctor: Doctor,
        licenseNames: List<String>
    ) {
        // 의사와 자격면허 관계 (N:M) 설정
        if (licenseNames.isNotEmpty()) { // 자격면허 이름 리스트가 비어있지 않은 경우에만 처리
            val doctorLicenses = licenseNames
                .mapNotNull { licenseName ->
                // 자격면허 이름을 기준으로 EducationLicense 엔티티 조회
                // 존재하지 않으면 새로 생성하여 저장
                val license = educationLicenseRepository.findByName(licenseName)
                    ?: educationLicenseRepository.save(EducationLicense(name = licenseName))

                // 해당 의사-자격면허 관계가 이미 존재하는지 확인
                val exists = doctorEducationLicenseRepository
                    .existsByDoctorIdAndEducationLicenseId(savedDoctor.id, license.id!!)

                if (!exists) {
                    // 중복되지 않은 경우에만 DoctorEducationLicense 객체 생성
                    DoctorEducationLicense(
                        doctor = savedDoctor,  // 저장된 의사 객체
                        educationLicense = license // 조회 또는 생성된 자격면허 객체
                    )
                } else {
                    null // 이미 존재하는 관계는 저장하지 않음
                }
            }.filterNotNull() // null 값을 제거

            // 생성된 의사-자격면허 관계 리스트를 DB에 일괄 저장
            doctorEducationLicenseRepository.saveAll(doctorLicenses) 
        }
    }

    // 병원과 의사 관계(HospitalDoctor)를 저장
    private fun linkDoctorToHospital(
        savedDoctor: Doctor,
        savedHospital: Hospital
    ) {
        // 해당 병원-의사 관계가 이미 존재하는지 확인
        val existsHospitalDoctor = hospitalDoctorRepository.existsByHospitalIdAndDoctorId(savedHospital.id, savedDoctor.id)
        if (!existsHospitalDoctor) {
            // 존재하지 않을 경우에만 새로운 관계 새성
            val hospitalDoctor = HospitalDoctor(
                hospital = savedHospital, // 현재 저장된 병원 엔티티
                doctor = savedDoctor // 현재 저장된 의사 엔티티
            )

            // 병원-의사 관계 저장
            hospitalDoctorRepository.save(hospitalDoctor)
        }
    }

    // 병원에 소속된 의사 정보 전체를 저장하고 관계를 연결하는 함수
    private fun saveDoctorsForHospital(
        doctors: List<Map<String, String?>>,
        savedHospital: Hospital
    ) {
        doctors.forEach { doctorData ->
            val savedDoctor = createOrUpdateDoctor(doctorData)

            // null인 경우 처리 중단
            if (savedDoctor == null) return@forEach

            // 진료과 이름들 (문자열 → 리스트로 변환, 예: "내과, 정형외과"))
            val specialtyNames = (doctorData["specialty"] as? String)
                ?.split(", ") // ", " 기준으로 나누기
                ?: emptyList() // 값이 없으면 빈 리스트
            // 의사와 진료과의 관계(DoctorSpecialty)를 저장
            saveDoctorSpecialties(savedDoctor, specialtyNames)

            // 경력 정보 리스트 (쉼표로 구분된 문자열을 리스트로 변환하여 공백 제거)
            val careerNames = (doctorData["career"] as? String)
                ?.split(", ") // ", " 기준으로 나누기
                ?.map { it.trim() } // 앞뒤 공백 제거
                ?.filter { it.isNotEmpty() } // 빈 문자열 제거
                ?: emptyList() // 값이 없으면 빈 리스트
            // 의사와 경력의 관계(DoctorCareer)를 저장
            saveDoctorCareers(savedDoctor, careerNames)

            // 자격면허 리스트 (쉼표로 구분된 문자열을 리스트로 변환하여 공백 제ㅓ거)
            val licenseNames = (doctorData["educationLicense"] as? String)
                ?.split(", ") // ", " 기준으로 나누기
                ?.map { it.trim() } // 앞뒤 공백 제거 
                ?.filter { it.isNotEmpty() } // 빈 문자열 제거
                ?: emptyList() // 값이 없으면 빈 리스트
            saveDoctorEducationLicenses(savedDoctor, licenseNames)    
            
            linkDoctorToHospital(savedDoctor, savedHospital)
        }
    }

    // 병원 부가 정보 저장 및 병원과의 관계설정
    private fun saveHospitalAdditionalInfo(
        savedHospital: Hospital, // 저장된 병원 객체
        additionalInfo: Map<String, Any> // 부가 정보 Map
    ) {
        // Map으로부터 각 필드를 추출하여 AdditionalInfo 엔티티 생성
        val additionalInfoEntity = AdditionalInfo(
            open24Hours = additionalInfo["open24Hours"] as? Boolean ?: false, // 24시간 운영 여부
            emergencyTreatment = additionalInfo["emergencyTreatment"] as? Boolean ?: false, // 응급 진료 여부
            maleFemaleDoctorChoice = additionalInfo["maleFemaleDoctorChoice"] as? Boolean ?: false, // 남녀 전문의 선택 가능 여부
            networkHospital = additionalInfo["networkHospital"] as? Boolean ?: false, // 네트워크 병원 여부
            freeCheckup = additionalInfo["freeCheckup"] as? Boolean ?: false, // 무료 검진 여부
            nearSubway = additionalInfo["nearSubway"] as? Boolean ?: false, // 역세권 여부
            openAllYear = additionalInfo["openAllYear"] as? Boolean ?: false, // 연중무휴 여부
            openOnSunday = additionalInfo["openOnSunday"] as? Boolean ?: false, // 일요일 진료 여부 
            nightShift = additionalInfo["nightShift"] as? Boolean ?: false, // 야간 진료 여부
            collaborativeCare = additionalInfo["collaborativeCare"] as? Boolean ?: false, // 협진 시스템 여부
            noLunchBreak = additionalInfo["noLunchBreak"] as? Boolean ?: false // 점심시간 진료 여부
        )

        // 생성한 추가 정보 엔티티를 DB에 저장
        val savedAdditionalInfo = additionalInfoRepository.save(additionalInfoEntity)

        // 병원 ID와 병원 부가 정보를 연결하여 HospitalAdditionalInfo 엔티티에 저장
        val hospitalAdditionalInfo = HospitalAdditionalInfo(
            id = savedHospital.id, // 병원 ID를 식별자로 사용
            hospital = savedHospital, // 병원 엔티티 참조
            additionalInfo = savedAdditionalInfo // 저장된 부가 정보 참조
        )

        // 병원-추가 정보 관계 저장
        hospitalAdditionalInfoRepository.save(hospitalAdditionalInfo)
    }

    // 주소를 위도/경도로 변환 (네이버 Geolocation API 사용)
    private fun getCoordinatesFromAddress(
        address: String
    ): Pair<Double, Double>? {
        return try {
            // 비동기 방식의 WebClient 호출 결과를 block()을 통해 동기 방식으로 받음
            val response = geolocation.getGeocode(address).block() 

            // 응답받은 JSON 문자열을 Jackson의 ObjectMapper를 이용해 파싱
            val jsonNode = ObjectMapper().readTree(response)
            
            // 주소 결과 리스트 추출
            val addresses = jsonNode["addresses"]
            if (addresses != null && addresses.isArray && addresses.size() > 0) {
                // 첫 번째 주소 결과에서 위도(y)와 경도(x) 추출
                val firstResult = addresses[0]
                val latitude = firstResult["y"]?.asDouble() ?: return null // 위도
                val longitude = firstResult["x"]?.asDouble() ?: return null // 경도

                // 위도, 경도를 쌍으로 반환
                Pair(latitude, longitude)
            } else {
                // 주소 반환 결과가 없는 경우 로그 출력 후 null 반환
                println("❌ address convertion to coordinates failed: no result")
                null
            }
        } catch (e: Exception) {
            // 예외 발생 시 로그 출력 후 null 반환
            println("❌ address convertion exception occured: ${e.message}")
            null
        }
    }

    fun createPoint(latitude: Double, longitude: Double): Point {
        val geometryFactory = GeometryFactory(PrecisionModel(), 4326)
        val point = geometryFactory.createPoint(Coordinate(longitude, latitude))
        point.srid = 4326 // Kotlin에서 unresolved reference가 발생한다면 setSRID() 사용
        return point
    }
    
    

    // 주소를 기반으로 좌표를 조회한 후, 병원 엔티티에 위치 정보를 설정하고 저장
    private fun setHospitalLocationFromAddress(savedHospital: Hospital, address: String) {
        val coordinates = getCoordinatesFromAddress(address)
        if (coordinates != null) {
            val (latitude, longitude) = coordinates
            val point = createPoint(latitude, longitude)
            savedHospital.location = point
            hospitalRepository.save(savedHospital) // 좌표까지 포함한 병원 정보 저장
        } else {
            println("⚠️ Failed to set location for hospital: ${savedHospital.id} - coordinate convertion failed")
        }
    }

    @Transactional
    fun saveHospital(
        id: String, // 병원 ID (예: H0000123456)
        name: String, // 병원명
        phoneNumber: String?, // 병원 홈페이지 URL (선택적 정보)
        homepage: String?, // 병원 홈페이지 URL (선택적 정보)
        address: String, // 병원 주소
        operatingHours: String?, // 병원 운영 시간 
        specialties: List<String>?, // 병원에서 운영하는 진료과 리스트
        url: String?, // 병원 상세 정보 페이지 URL
        additionalInfo: Map<String, Any>?, // 병원의 부가 정보
        doctors: List<Map<String, String?>>?, // 병원에 소속된 의사 정보 리스트
        hospitalImages: List<Image> // 병원과 연결된 이미지 리스트
    ): Hospital {
        // 병원 객체 생성 또는 업데이트 후 저장
        val savedHospital = createOrUpdateHospital(
            id = id,
            name = name,
            phoneNumber = phoneNumber,
            homepage = homepage,
            address = address,
            operatingHours = operatingHours,
            url = url
        )

        // 병원의 위치 정보 설정 
        setHospitalLocationFromAddress(savedHospital, address)

        // 병원의 진료과 정보 저장 (중복 방지 포함)
        saveHospitalSpecialties(savedHospital, specialties)

        // 병원의 의사 정보 저장 (새로운 의사 데이터 추가)
        if (!doctors.isNullOrEmpty()) {
            saveDoctorsForHospital(doctors, savedHospital)   
        }

        // 병원 이미지가 존재할 경우 병원-이미지 관계 저장 수행
        if (hospitalImages.isNotEmpty()) {
            // 이미지 정보와 병원 정보를 연계하여 저장
            saveHospitalImages(savedHospital, hospitalImages)
        }

        // 병원에 대한 추가 정보가 제공된 경우 처리
        if (additionalInfo != null) {
            saveHospitalAdditionalInfo(savedHospital, additionalInfo)
        }

        return savedHospital // 최종적으로 저장된 병원 엔티티 반환
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

    // 병원 ID로 병원 정보 조회
    fun getHospitalById(id: String): Hospital {
        return hospitalRepository.findById(id)
            .orElseThrow { NoSuchElementException("해당 ID의 병원이 존재하지 않습니다: $id") }
    }

    // 전체 병원 개수를 반환하는 함수
    fun countAllHospitals(): Long {
        return hospitalRepository.count()  // JPA의 count() 메서드를 사용하여 전체 개수 반환
    }

    // 병원 위치를 기준으로 가까운 병원 순으로 정렬
    fun getHospitalsByLocation(location: Point, pageable: Pageable): Page<Hospital> {
        return hospitalRepository.findAllByLocationOrderByDistance(location, pageable)
    }

    fun getHospitalsByFilters(
        location: Point?, // 위치 정보
        maxDistanceInKm: Double?, // 거리 제한 (km 단위)
        specialties: List<String>?, // 진료과 리스트
        sortBy: String, // 정렬 기준 ("distance" 또는 "name")
        pageable: Pageable // 페이징 정보
    ): Page<Hospital> {
        // 사용자가 입력한 거리(km)를 m 단위로 변환 (예: 3km → 3000m)
        val maxDistanceInMeters = maxDistanceInKm?.times(1000) 

        // 진료과 필터링 값이 비어 있거나 null이면, null로 처리
        val safeSpecialties = if (specialties.isNullOrEmpty()) null else specialties

        // 정렬 유효성 검사 
        val validSortBy = when (sortBy.lowercase()) {
            "distance", "name" -> sortBy.lowercase() // 유효한 경우 그대로 사용
            else -> "distance" // 잘못된 값이 들어온 경우 기본적으로 "distance" 사용
        }

        // 필터링 조건을 기준으로 병원 목록 조회
        return hospitalRepository.searchHospitalsByFilters(
            location = location, // 기준 위치
            maxDistanceInMeters = maxDistanceInMeters, // 거리 제한 (m 단위)
            specialties = safeSpecialties, // 필터링할 진료과 목록
            sortBy = validSortBy, // 정렬 기준
            pageable = pageable // 페이지 요청 정보
        )
    }
}