package com.callrapport.service

// Model (엔티티) 관련 import
import com.callrapport.model.hospital.Hospital // Hospital: 병원 기본 정보 엔티티
import com.callrapport.model.hospital.HospitalDoctor // HospitalDoctor: 병원-의사 관계 엔티티
import com.callrapport.model.hospital.AdditionalInfo // AdditionalInfo: 병원 관련 부가 정보 엔티티
import com.callrapport.model.hospital.HospitalImage
import com.callrapport.model.hospital.HospitalAdditionalInfo // HospitalAdditionalInfo: 병원-병원 관련 부가 정보 연결 엔티티
import com.callrapport.model.hospital.HospitalSpecialty // HospitalSpecialty: 병원-진료과 연결 엔티티
import com.callrapport.model.common.Specialty // Specialty: 진료과 엔티티
import com.callrapport.model.common.Image
import com.callrapport.model.doctor.Doctor // Doctor: 의사 정보 엔티티
import com.callrapport.model.doctor.DoctorSpecialty
import com.callrapport.model.doctor.EducationLicense
import com.callrapport.model.doctor.DoctorEducationLicense
import com.callrapport.model.doctor.Career
import com.callrapport.model.doctor.DoctorCareer

// Repository (저장소) 관련 import
import com.callrapport.repository.hospital.HospitalRepository // HospitalRepository: 병원 정보 저장소
import com.callrapport.repository.hospital.AdditionalInfoRepository // AdditionalInfoRepository: 병원 부가 정보 저장소
import com.callrapport.repository.hospital.HospitalImageRepository
import com.callrapport.repository.hospital.HospitalDoctorRepository // HospitalDoctorRepository: 병원-의사 관계 저장소
import com.callrapport.repository.hospital.HospitalAdditionalInfoRepository // HospitalAdditionalInfoRepositor: 병원-병원 부가 정보 관계 저장소
import com.callrapport.repository.hospital.HospitalSpecialtyRepository // HospitalSpecialtyRepository: 병원-진료과 관계 저장소 
import com.callrapport.repository.common.SpecialtyRepository // SpecialtyRepository: 진료과 정보 저장소
import com.callrapport.repository.common.ImageRepository
import com.callrapport.repository.doctor.DoctorRepository // DoctorRepository: 의사 정보 저장소 
import com.callrapport.repository.doctor.EducationLicenseRepository
import com.callrapport.repository.user.UserFavoriteHospitalRepository // UserFavoriteHospitalRepository
import com.callrapport.repository.doctor.DoctorSpecialtyRepository
import com.callrapport.repository.doctor.DoctorEducationLicenseRepository
import com.callrapport.repository.doctor.CareerRepository
import com.callrapport.repository.doctor.DoctorCareerRepository

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
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

// 공간 데이터(geo) 관련 import (병원 위치 좌표 관리)
import org.locationtech.jts.geom.Coordinate // 좌표 데이터 타입
import org.locationtech.jts.geom.GeometryFactory // 공간 데이터 객체 생성
import org.locationtech.jts.geom.Point // 병원 위치를 저장하는 Point 타입
import org.locationtech.jts.geom.PrecisionModel // 좌표 정밀도 설정

@Service
class HospitalService(
    private val hospitalRepository: HospitalRepository, // 병원 저장소
    private val additionalInfoRepository: AdditionalInfoRepository, // 병원 부가 정보 저장소
    private val hospitalDoctorRepository: HospitalDoctorRepository, // 병원-의사 관계 저장소
    private val hospitalAdditionalInfoRepository: HospitalAdditionalInfoRepository, // 병원-병원 부가 정보 관계 저장소
    private val hospitalSpecialtyRepository: HospitalSpecialtyRepository, // 병원-진료과 연결 저장소
    private val hospitalImageRepository: HospitalImageRepository,
    private val specialtyRepository: SpecialtyRepository, // 진료과 저장소
    private val educationLicenseRepository: EducationLicenseRepository,
    private val doctorRepository: DoctorRepository, // 의사 저장소
    private val careerRepository: CareerRepository, 
    private val doctorSpecialtyRepository: DoctorSpecialtyRepository,
    private val doctorCareerRepository: DoctorCareerRepository,
    private val doctorEducationLicenseRepository: DoctorEducationLicenseRepository,
    private val userFavoriteHospitalRepository: UserFavoriteHospitalRepository,
    
    private val imageRepository: ImageRepository
) {
    @Transactional
    fun saveHospitalImages(hospital: Hospital, images: List<Image>) {
        images.forEach { image ->
            val existingImage = imageRepository.findByUrl(image.url)
            val savedImage = existingImage ?: imageRepository.save(image)

            // ✅ 병원-이미지 관계가 이미 존재하는지 확인
            val exists = hospitalImageRepository.existsByHospitalIdAndImageId(hospital.id, savedImage.id!!)
            if (!exists) {
                val hospitalImage = HospitalImage(
                    hospital = hospital,
                    image = savedImage
                )
                hospitalImageRepository.save(hospitalImage)
            } else {
                println("⚠️ Hospital-Image relation already exists: ${hospital.id} - ${savedImage.id}")
            }
        }
    }

    @Transactional
    fun saveHospital(
        id: String,
        name: String,
        phoneNumber: String?,
        homepage: String?,
        address: String,
        operatingHours: String?,
        specialties: List<String>?,
        url: String?,
        additionalInfo: Map<String, Any>?,
        doctors: List<Map<String, String?>>?,
        hospitalImages: List<Image>  
    ): Hospital {
        // 기존 병원 데이터 확인 (있으면 업데이트, 없으면 새로 생성)
        val existingHospital = hospitalRepository.findById(id).orElse(null)

        // 병원 객체 생성 또는 업데이트
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

         // ✅ 병원의 진료과 정보 저장 (중복 방지)
        if (!specialties.isNullOrEmpty()) {
            val specialtyEntities = specialties.distinct().mapNotNull { specialtyName ->
                val specialty = specialtyRepository.findByName(specialtyName)
                    ?: return@mapNotNull null // 존재하지 않는 진료과는 저장 X

                // ✅ 이미 존재하는 병원-진료과 관계인지 확인 후 중복 방지
                val exists = hospitalSpecialtyRepository.existsByHospitalIdAndSpecialtyId(savedHospital.id, specialty.id)
                if (!exists) {
                    HospitalSpecialty(specialty = specialty, hospital = savedHospital)
                } else {
                    null // 이미 존재하면 저장 X
                }
            }.filterNotNull() // Null 제거

            hospitalSpecialtyRepository.saveAll(specialtyEntities) // ✅ 중복 데이터 없이 저장!
        }
        

        // ✅ 병원의 의사 정보 저장 (새로운 추가)
        if (!doctors.isNullOrEmpty()) {
            doctors.forEach { doctorData ->
                val doctorId = doctorData["id"] as? String ?: return@forEach
                val doctorName = doctorData["name"] as? String ?: return@forEach
                val profileImage = doctorData["profileImage"] as? String
                val specialtyNames = (doctorData["specialty"] as? String)?.split(", ") ?: emptyList() // ✅ 여러 개의 진료과 처리
                val careerNames = (doctorData["career"] as? String)
                    ?.split(", ")
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?: emptyList()
                val licenseNames = (doctorData["educationLicense"] as? String)
                    ?.split(", ")
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?: emptyList()
                
                // 의사 정보 저장 (기존 정보가 있으면 업데이트, 없으면 새로 저장)
                val existingDoctor = doctorRepository.findById(doctorId).orElse(null)
                val doctor = if (existingDoctor != null) {
                    existingDoctor.copy(
                        name = doctorName,
                        profileImage = profileImage ?: existingDoctor.profileImage
                    )
                } else {
                    Doctor(
                        id = doctorId,
                        name = doctorName,
                        profileImage = profileImage
                    )
                }

                // 의사 정보를 doctorRepository에 저장
                val savedDoctor = doctorRepository.save(doctor)

                // ✅ 의사와 진료과 관계 (N:M) 설정 (✅ 수정된 부분)
                if (specialtyNames.isNotEmpty()) {
                    val doctorSpecialties = specialtyNames.distinct().mapNotNull { specialtyName -> 
                        val specialty = specialtyRepository.findByName(specialtyName)
                            ?: return@mapNotNull null

                        //  -이미 존재하는 의사-진료과 관계인지 확인
                        val exists = doctorSpecialtyRepository.existsByDoctorIdAndSpecialtyId(savedDoctor.id, specialty.id)
                        if (!exists) {
                            DoctorSpecialty(doctor = savedDoctor, specialty = specialty)
                        } else {
                            null // 이미 존재하면 저장하지 않음
                        }
                    }.filterNotNull() // Null 값 제거

                    doctorSpecialtyRepository.saveAll(doctorSpecialties) // ✅ 중복 데이터 없이 저장
                }          
                
                // 의사와 경력 관계 (N:M) 설정
                if (!careerNames.isNullOrEmpty()) {
                    val doctorCareers = careerNames.distinct().mapNotNull { careerName ->
                        val career = careerRepository.findByName(careerName)
                            ?: careerRepository.save(Career(name = careerName))

                        // 의사-경력 관계가 이미 존재하는지 확인
                        val exists = doctorCareerRepository.existsByDoctorIdAndCareerId(savedDoctor.id, career.id!!)
                        if (!exists) {
                            DoctorCareer(doctor = savedDoctor, career = career)
                        } else {
                            null // 이미 존재하면 저장하지 않음
                        }
                    }.filterNotNull() // Null 값 제거

                    doctorCareerRepository.saveAll(doctorCareers)
                }

                // ✅ 의사와 자격면허 관계 (N:M) 설정
                if (licenseNames.isNotEmpty()) {
                    val doctorLicenses = licenseNames.mapNotNull { licenseName ->
                        val license = educationLicenseRepository.findByName(licenseName)
                            ?: educationLicenseRepository.save(EducationLicense(name = licenseName))

                        // 의사-자격면허 관계가 이미 존재하는지 확인
                        val exists = doctorEducationLicenseRepository.existsByDoctorIdAndEducationLicenseId(savedDoctor.id, license.id!!)
                        if (!exists) {
                            DoctorEducationLicense(doctor = savedDoctor, educationLicense = license)
                        } else {
                            null // 이미 존재하면 저장하지 않음
                        }
                    }.filterNotNull() // Null 값을 제거

                    doctorEducationLicenseRepository.saveAll(doctorLicenses) // ✅ 중복 데이터 없이 저장
                }


                // ✅ 병원-의사 관계 중복 방지
                val existsHospitalDoctor = hospitalDoctorRepository.existsByHospitalIdAndDoctorId(savedHospital.id, savedDoctor.id)
                if (!existsHospitalDoctor) {
                    val hospitalDoctor = HospitalDoctor(hospital = savedHospital, doctor = savedDoctor)
                    hospitalDoctorRepository.save(hospitalDoctor)
                }
            }
        }

        if (hospitalImages.isNotEmpty()) {
            saveHospitalImages(savedHospital, hospitalImages)
        }

        // ✅ 추가 정보가 제공되었으면 AdditionalInfo 저장
        if (additionalInfo != null) {
            val additionalInfoEntity = AdditionalInfo(
                open24Hours = additionalInfo["open24Hours"] as? Boolean ?: false,
                emergencyTreatment = additionalInfo["emergencyTreatment"] as? Boolean ?: false,
                maleFemaleDoctorChoice = additionalInfo["maleFemaleDoctorChoice"] as? Boolean ?: false,
                networkHospital = additionalInfo["networkHospital"] as? Boolean ?: false,
                freeCheckup = additionalInfo["freeCheckup"] as? Boolean ?: false,
                nearSubway = additionalInfo["nearSubway"] as? Boolean ?: false,
                openAllYear = additionalInfo["openAllYear"] as? Boolean ?: false,
                openOnSunday = additionalInfo["openOnSunday"] as? Boolean ?: false,
                nightShift = additionalInfo["nightShift"] as? Boolean ?: false,
                collaborativeCare = additionalInfo["collaborativeCare"] as? Boolean ?: false,
                noLunchBreak = additionalInfo["noLunchBreak"] as? Boolean ?: false
            )

            // 병원 부가 정보를 데이터베이스에 저장
            val savedAdditionalInfo = additionalInfoRepository.save(additionalInfoEntity)

            // 병원 ID와 병원 부가 정보를 연결하여 HospitalAdditionalInfo 엔티티에 저장
            val hospitalAdditionalInfo = HospitalAdditionalInfo(
                id = savedHospital.id,
                hospital = savedHospital,
                additionalInfo = savedAdditionalInfo
            )

            hospitalAdditionalInfoRepository.save(hospitalAdditionalInfo)
        }

        return savedHospital
    }

    // 주소를 기반으로 위도/경도를 조회 (카카오맵 API 사용)
    fun getCoordinatesFromAddress(address: String): Pair<Double, Double>? {
        val apiKey = "b66445a2658c58be46ba36fef5748c4f" // REST API 키 사용
        val restTemplate = RestTemplate() // RestTemplate 인스턴스 생성

        // 카카오맵 API의 주소 검색 엔드포인트 URL 생성
        val url = UriComponentsBuilder.fromHttpUrl("https://dapi.kakao.com/v2/local/search/address.json")
            .queryParam("query", address) // query 파라미터에 변환할 주소 추가
            .build()
            .toUriString()        

        return try {
            // HTTP 요청 헤더 설정 
            val headers = org.springframework.http.HttpHeaders()
            headers.set("Authorization", "KakaoAK $apiKey") // 인증 키 추가
            val entity = HttpEntity<String>(headers)

            // REST API 호출 (GET 요청)
            val response: ResponseEntity<String> = restTemplate.exchange(url, HttpMethod.GET, entity, String::class.java)
            
            // JSON 데이터 파싱
            val objectMapper = ObjectMapper()
            val jsonNode: JsonNode = objectMapper.readTree(response.body)
            val documents = jsonNode["documents"]

            if (documents != null && documents.isArray && documents.size() > 0) {
                val location = documents[0]
                val latitude = location["y"]?.asDouble() ?: return null // 위도 (y 값)
                val longitude = location["x"]?.asDouble() ?: return null // 경도 (x 값)
                Pair(latitude, longitude)
            } else {
                println("get coordinates failed: no result")
                null
            }
        } catch (e: Exception) {
            println("get coordinates failed: ${e.message}")
            null
        }
    }
    
    // 위도와 경도를 기반으로 Point 객체를 생성하는 함수
    fun createPoint(latitude: Double, longitude: Double): Point {
        val geometryFactory = GeometryFactory(PrecisionModel(), 4326) // SRID 4326 (WGS 84) 사용
        return geometryFactory.createPoint(Coordinate(longitude, latitude))
    }

    @Transactional
    fun deleteAllHospitalData() {
        val hospitals = hospitalRepository.findAll()

        hospitals.forEach { hospital ->
            hospital.specialties.clear()
            hospital.doctors.clear()
            hospital.favoritedByUsers.clear()
        }

        hospitalRepository.saveAll(hospitals) // 변경된 상태를 반영

        hospitalDoctorRepository.deleteAll()
        hospitalSpecialtyRepository.deleteAll()
        hospitalAdditionalInfoRepository.deleteAll()
        additionalInfoRepository.deleteAll()
        userFavoriteHospitalRepository.deleteAll()

        hospitalRepository.deleteAll()
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
