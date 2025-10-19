package com.callrapport.service

// Component (컴포넌트) 관련 import
import com.callrapport.component.crawler.doctor.FamousDoctorCrawler // 명의 크롤러 컴포넌트

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

// 공간 데이터 관련 import
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.PrecisionModel

@Service
class DoctorService(
    // 크롤러: 명의 정보 크롤링
     private val famousDoctorCrawler: FamousDoctorCrawler, // 명의 크롤러 서비스

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

    private fun saveDoctorEducationLicenses(savedDoctor: Doctor, educationLicenseNames: List<String>?) {
        // 함수 시작 시점에 바로 중복을 제거
        val uniqueLicenseNames = educationLicenseNames?.distinct()

        if (!uniqueLicenseNames.isNullOrEmpty()) { // 중복이 제거된 리스트로 로직 수행
            uniqueLicenseNames.forEach { licenseName ->
                    val license = educationLicenseRepository.findByName(licenseName)
                        ?: educationLicenseRepository.save(EducationLicense(name = licenseName))

                    doctorEducationLicenseRepository.saveWithIgnore(savedDoctor.id!!, license.id!!)
                }
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

    // 전체 의사 개수를 반환하는 함수
    fun countAllDoctors(): Long {
        return doctorRepository.count()  // JPA의 count() 메서드를 사용하여 전체 개수 반환
    }

    fun getDoctorsByFilters(
        keyword: String?,
        specialtyNames: List<String>?, // <<< 1. specialtyNames 파라미터 추가
        location: Point?,
        sortBy: String,
        pageable: Pageable
    ): Page<Doctor> {
        // <<< 2. 빈 리스트가 넘어올 경우를 대비해 null로 처리
        val finalSpecialtyNames = if (specialtyNames.isNullOrEmpty()) null else specialtyNames

        return doctorRepository.searchDoctorsByFilters(
            keyword = keyword,
            specialtyNames = finalSpecialtyNames, // <<< 3. 레포지토리에 파라미터 전달
            location = location,
            sortBy = sortBy,
            pageable = pageable
        )
    }

    fun crawlAndFetchFamousDoctors(): List<Map<String, String>> {
        println("--- Service layer: Starting famous doctor crawl process ---")
        val crawledData = famousDoctorCrawler.crawlFamousDoctors()
        println("--- Service layer: Crawl process finished, returning data to controller ---")
        return crawledData
    }

    /**
     * [신규 추가] 크롤링된 데이터에서 모든 'specialtyName'을 추출하여 중복 없이 반환하는 분석 함수.
     * 자체 검수 목적으로 사용됨.
     */
    fun analyzeCrawledSpecialties(): List<String> {
        println("--- Service layer: Analyzing crawled specialty names... ---")
        // 크롤러를 호출하여 원본 데이터 리스트를 가져옵니다.
        val crawledData = famousDoctorCrawler.crawlFamousDoctors()

        // specialtyName을 쉼표로 분리하고, 모든 공백을 제거한 뒤, 중복을 없애고 정렬
        val uniqueSpecialties = crawledData
            .flatMap { it["specialtyName"]?.split(",") ?: emptyList() }
            .map { it.trim() }
            .toSet() // Set을 사용하여 중복 제거
            .sorted() // 정렬

        println("--- Service layer: Analysis complete. Found ${uniqueSpecialties.size} unique specialties. ---")
        return uniqueSpecialties
    }

    /**
     * 명의 크롤링부터 DB 업데이트까지 모든 과정을 총괄하는 함수
     * @return 업데이트된 의사 엔티티 목록
     */
    @Transactional
    fun updateFamousDoctors(): List<Doctor> { 
        println("--- Starting famous doctor update process ---")

        // 1단계: 최신 명의 목록 크롤링
        val famousDoctorsData = famousDoctorCrawler.crawlFamousDoctors()
        println("Crawling complete. Found ${famousDoctorsData.size} raw doctor data entries.")

        // 2단계: DB에서 조회할 이름 목록 준비 (중복 제거)
        val processedData = famousDoctorsData.distinctBy { it["name"] to it["hospitalName"] to it["specialtyName"] }
        val crawledNames = processedData.mapNotNull { it["name"] }.distinct()
        
        // 3단계: Repository에서 이름이 일치하는 모든 후보 의사를 한 번에 가져옴 (HospitalDoctorRepository 함수는 별도 파일에 추가되었음을 가정)
        val candidateDoctors = doctorRepository.findByNameIn(crawledNames) // <<< findByNameIn 사용
        val candidateDoctorIds = candidateDoctors.map { it.id }
        
        // 후보 의사들의 병원 연결 정보를 메모리로 가져옴
        val hospitalRelations = hospitalDoctorRepository.findByDoctorIdIn(candidateDoctorIds)
            .groupBy { it.doctor.id }

        // 4단계: 메모리에서 데이터 가공 및 최종 매칭 (동명이인 구분 로직)
        val doctorsToUpdate = mutableListOf<Doctor>()
        println("Processing ${processedData.size} unique famous doctors.")

        processedData.forEach { data ->
            val crawledName = data["name"] ?: return@forEach
            // 공백 제거: 띄어쓰기 오류 방지
            val crawledHospital = data["hospitalName"]?.replace(" ", "") ?: return@forEach 
            // 매핑된 진료과 목록 (쉼표로 분리되어 있음)
            val crawledSpecialties = data["specialtyName"]?.split(",")?.map { it.trim() } ?: return@forEach 

            // 이름이 일치하는 후보 의사 필터링 (동명이인 처리 시작)
            val nameCandidates = candidateDoctors.filter { it.name == crawledName }

            if (nameCandidates.isEmpty()) {
                println("FAILED: Name=$crawledName, Hospital=$crawledHospital. Reason: Doctor name not found in DB.")
                return@forEach
            }
            
            var matchSuccess = false
            for (candidate in nameCandidates) {
                
                // 1. 병원명 일치 여부 확인 (공백 제거 후 부분 포함 비교)
                val dbHospitals = hospitalRelations[candidate.id]
                    ?.map { it.hospital.name.replace(" ", "") } ?: emptyList()
                val isHospitalMatch = dbHospitals.any { it.contains(crawledHospital) || crawledHospital.contains(it) }

                // 2. 진료과 일치 여부 확인 (매핑/포함 관계 비교)
                val dbSpecialties = candidate.specialties.map { it.specialty.name }
                val isSpecialtyMatch = dbSpecialties.any { dbSpec ->
                    crawledSpecialties.any { crawledSpec ->
                        dbSpec.contains(crawledSpec) || crawledSpec.contains(dbSpec) 
                    }
                }
                
                if (isHospitalMatch && isSpecialtyMatch) {
                    // 5단계: 업데이트 (명의 여부만 변경)
                    candidate.isFamous = true
                    doctorsToUpdate.add(candidate)
                    println("SUCCESS: Matched doctor: ${candidate.name} at ${data["hospitalName"]}")
                    matchSuccess = true
                    break // 매칭 성공 시 동명이인 루프 종료
                }
            }
            
            // 6단계: 상세 실패 이유 로깅 (매칭 실패 시)
            if (!matchSuccess) {
                // 실패한 이유를 찾기 위해 후보군을 다시 분석 (디버그 용도)
                val isAnyHospitalMatch = nameCandidates.any { candidate ->
                    val dbHospitals = hospitalRelations[candidate.id]?.map { it.hospital.name.replace(" ", "") } ?: emptyList()
                    dbHospitals.any { it.contains(crawledHospital) || crawledHospital.contains(it) }
                }
                val isAnySpecialtyMatch = nameCandidates.any { candidate ->
                    val dbSpecialties = candidate.specialties.map { it.specialty.name }
                    dbSpecialties.any { dbSpec ->
                        crawledSpecialties.any { crawledSpec ->
                            dbSpec.contains(crawledSpec) || crawledSpec.contains(dbSpec)
                        }
                    }
                }

                val failureReason = when {
                    !isAnyHospitalMatch && !isAnySpecialtyMatch -> "Reason: Both Hospital and Specialty Match Failed."
                    !isAnyHospitalMatch -> "Reason: Hospital Match Failed for all Candidates."
                    !isAnySpecialtyMatch -> "Reason: Specialty Match Failed for all Candidates."
                    else -> "Reason: Unforeseen Logic/Ambiguity Error."
                }
                println("FAILED: Name=$crawledName, Hospital=${data["hospitalName"]}. $failureReason")
            }
        }

        // 7단계: 변경된 의사 정보를 '한번에' DB에 저장
        val distinctUpdatedDoctors = doctorsToUpdate.distinctBy { it.id }
        if (distinctUpdatedDoctors.isNotEmpty()) {
            doctorRepository.saveAll(distinctUpdatedDoctors)
        }
        
        println("--- Famous doctor update process finished. Total ${distinctUpdatedDoctors.size} records updated. ---")
        
        return distinctUpdatedDoctors
    }
}
