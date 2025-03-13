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
import com.callrapport.repository.user.UserFavoriteHospitalRepository // UserFavoriteHospitalRepository
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
    private val specialtyRepository: SpecialtyRepository, // 진료과 저장소
    private val doctorRepository: DoctorRepository, // 의사 저장소
    private val userFavoriteHospitalRepository: UserFavoriteHospitalRepository
) {
    @Transactional
    fun saveHospitalWithoutCoordinates(
        id: String, // 병원 ID
        name: String, // 병원 이름
        phoneNumber: String?, // 병원 전화번호
        homepage: String?, // 병원 홈페이지
        address: String, // 병원 주소 (누락된 부분 추가)
        operatingHours: String?, // 병원 운영 시간
        specialties: List<String>?, // 병원 진료과 리스트
        url: String?, // 병원 URL
        additionalInfo: Map<String, Any>?, // 병원 부가 정보
    ): Hospital {
        // 기존 병원 데이터 확인 (있으면 업데이트, 없으면 새로 생성)
        val existingHospital = hospitalRepository.findById(id).orElse(null)

        // 좌표 저장 X
        val location: Point? = null

        // 병원 데이터가 이미 존재하는지 확인
        val hospital = if (existingHospital != null) {
            // 기존 병원 데이터가 존재하면, 해당 데이터를 복사하면서 정보를 업데이트
            existingHospital.copy(
                name = name, // 병원 이름
                phoneNumber = phoneNumber, // 병원 전화번호
                homepage = homepage, // 병원 홈페이지
                address = address, // 병원 주소
                operatingHours = operatingHours, // 병원 운영시간
                url = url, // 병원 URL
                location = location
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
                url = url, // 병원 URL
                location = location
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
    additionalInfo: Map<String, Any>?, // 병원 부가 정보
    doctors: List<Map<String, String?>>? // ✅ 병원에 등록된 의사 목록 추가
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

    // ✅ 병원의 진료과 정보 저장
    if (!specialties.isNullOrEmpty()) {
        val specialtyEntities = specialties.map { specialtyName ->
            val specialty = specialtyRepository.findByName(specialtyName)
                ?: specialtyRepository.save(Specialty(name = specialtyName))

            HospitalSpecialty(specialty = specialty, hospital = savedHospital)
        }
        hospitalSpecialtyRepository.saveAll(specialtyEntities)
    }

    // ✅ 병원의 의사 정보 저장 (새로운 추가)
    if (!doctors.isNullOrEmpty()) {
        doctors.forEach { doctorData ->
            val doctorId = doctorData["id"] as? String ?: return@forEach
            val doctorName = doctorData["name"] as? String ?: return@forEach
            val specialtyName = doctorData["specialty"] as? String

            // 의사의 진료과를 조회하거나 생성
            val specialty = specialtyName?.let { specialtyRepository.findByName(it) ?: specialtyRepository.save(Specialty(name = it)) }

            // 의사 정보 저장 (기존 정보가 있으면 업데이트, 없으면 새로 저장)
            val existingDoctor = doctorRepository.findById(doctorId).orElse(null)
            val doctor = if (existingDoctor != null) {
                existingDoctor.copy(
                    name = doctorName,
                    specialty = specialty
                )
            } else {
                Doctor(
                    id = doctorId,
                    name = doctorName,
                    specialty = specialty
                )
            }

            // 의사 정보를 doctorRepository에 저장
            val savedDoctor = doctorRepository.save(doctor)

            // 병원과 의사의 관계를 저장
            val hospitalDoctor = HospitalDoctor(
                hospital = savedHospital,
                doctor = savedDoctor
            )
            hospitalDoctorRepository.save(hospitalDoctor) // 병원-의사 관계 저장
        }
    }

    // ✅ 추가 정보가 제공되었으면 AdditionalInfo 저장
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
