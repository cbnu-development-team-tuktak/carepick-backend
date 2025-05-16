package com.callrapport.service.disease

// Component 관련 import
import com.callrapport.component.file.FileManager // CSV 파일 읽기 컴포넌트

// Model (엔티티) 관련 import
import com.callrapport.model.disease.* // Disease, DiseaseCategory, DiseaseBodySystem, DiseaseSpecialty, Category, BodySystem
import com.callrapport.model.common.Specialty // 진료과 정보

// Repository (저장소) 관련 import
import com.callrapport.repository.disease.* // Disease 관련 리포지토리
import com.callrapport.repository.common.SpecialtyRepository // Specialty 리포지토리

// Spring 및 JPA 관련 import
import org.springframework.stereotype.Service // 서비스 컴포넌트 등록
import org.springframework.transaction.annotation.Transactional // 트랜잭션 처리 어노테이션
import org.springframework.data.domain.Page // 페이징 응답 객체
import org.springframework.data.domain.Pageable // 페이징 요청 객체

// Java 표준 라이브러리 관련 import
import java.nio.charset.Charset // 파일 인코딩 지정

@Service
class DiseaseService(
    private val fileManager: FileManager, // 파일(.csv 등)을 처리하는 유틸리티 컴포넌트
    private val diseaseRepository: DiseaseRepository, // 질병(Disease) 엔티티 저장/조회용 리포지토리
    private val categoryRepository: CategoryRepository, // 분류(Category) 엔티티 저장/조회용 리포지토리
    private val bodySystemRepository: BodySystemRepository, // 신체계통(BodySystem) 엔티티 저장/조회용 리포지토리
    private val specialtyRepository: SpecialtyRepository, // 진료과(Specialty) 엔티티 저장/조회용 리포지토리
    private val diseaseCategoryRepository: DiseaseCategoryRepository, // 질병-분류 관계(DiseaseCategory) 저장용 리포지토리
    private val diseaseBodySystemRepository: DiseaseBodySystemRepository, // 질병-신체계통 관계(DiseaseBodySystem) 저장용 리포지토리
    private val diseaseSpecialtyRepository: DiseaseSpecialtyRepository // 질병-진료과 관계(DiseaseSpecialty) 저장용 리포지토리
) {
    // CSV에서 추출한 한 줄의 구조화된 데이터
    private data class ParsedDiseaseRow(
        val categoryName: String, // '분류' 열 (예: 호흡기 질환)
        val diseaseName: String, // '질병명' 열 (예: 감기)
        val specialtyNames: List<String>, // '진료과' 열을 '/'로 분할한 리스트 (예: [내과, 가정의학과])
        val bodySystemName: String // '신체계통' 열
    )

    // CSV 한 행에서 필요한 데이터를 추출하여 구조화된 객체로 반환
    private fun parseCsvRow(
        row: Map<String, String> // CSV 한 행으로, 열 이름을 키로 갖는 Map 형태
    ): ParsedDiseaseRow? {
        val values = row.values.toList()

        // 컬럼 개수가 부족하면 건너뜀
        if (values.size < 4) return null

        // 분류명 추출 (예: 호흡기 질환)
        val categoryName = values[0].trim().replace("\"", "")

        // 질병명 추출 (예: 감기)
        val diseaseName = values[1].trim().replace("\"", "")

        // 진료과 추출 (예: [내과, 가정의학과])
        val specialtyNames = values[2].split("/").map { it.trim() }

        // 신체계통명 추출 (예: 호흡기)
        val bodySystemName = values[3].trim().replace("\"", "")

        return ParsedDiseaseRow(
            categoryName = categoryName, // 추출된 분류명
            diseaseName = diseaseName, // 추출된 질병명
            specialtyNames = specialtyNames, // 추출된 진료과 목록
            bodySystemName = bodySystemName // 추출된 신체계통명
        )
    }


    // 질병-분류 관계를 생성 및 저장
    private fun saveDiseaseCategory(
        disease: Disease, // 질병 엔티티
        categoryName: String // 분류명
    ) {
        // 분류명이 이미 DB에 존재하는지 확인
        val existingCategory = categoryRepository.findByName(categoryName)

        // 존재하지 않으면 분류 엔티티 새로 생성 후 저장
        val category = existingCategory ?: categoryRepository.save(
            Category(name = categoryName)
        )

        // 질병-분류 관계 저장
        diseaseCategoryRepository.save(
            DiseaseCategory(
                disease = disease, // 연관된 질병 엔티티
                category = category // 연관된 분류 엔티티
            )
        )
    }

    // 질병-신체계통 관계를 생성 및 저장
    private fun saveDiseaseBodySystem(
        disease: Disease, // 질병 엔티티
        bodySystemName: String // 신체계통명
    ) {
        // 신체계통명이 이미 DB에 존재하는지 확인
        val existingBodySystem = bodySystemRepository.findByName(bodySystemName)

        // 존재하지 않으면 신체계통 엔티티 새로 생성 후 저장
        val bodySystem = existingBodySystem ?: bodySystemRepository.save(
            BodySystem(name = bodySystemName)
        )

        // 질병-신체계통 관계 저장
        diseaseBodySystemRepository.save(
            DiseaseBodySystem(
                disease = disease, // 연관된 질병
                bodySystem = bodySystem // 연관된 신체계통
            )
        )
    }

    // 질병-진료과 관계를 생성 및 저장
    private fun saveDiseaseSpecialties(
        disease: Disease, // 연관할 질병 엔티티
        specialtyNames: List<String> // 진료과 이름 목록
    ) {
        // 진료과 이름이 비어 있으면 저장하지 않음
        if (specialtyNames.isEmpty()) return

        // 진료과 이름 목록을 기반으로 DiseaseSpecialty 리스트 생성
        val specialties = specialtyNames.mapNotNull { specialtyName ->
            val specialty = specialtyRepository.findByName(specialtyName)
                ?: return@mapNotNull null 

            DiseaseSpecialty(
                disease = disease, // 연관된 질병
                specialty = specialty // 연관된 진료과
            )
        }

        // 생성된 관계를 일괄 저장
        diseaseSpecialtyRepository.saveAll(specialties)
    }

    // CSV 파일을 읽어 질병 및 관련 엔티티들을 DB에 저장
    @Transactional
    fun saveDiseasesFromCsv(
        filePath: String // 읽어올 CSV 파일 경로
    ) {
        println("[INFO] Starting to read CSV file: $filePath")

        // CSV 파일을 읽어 각 행을 Map<String, String> 형태로 반환
        val rows = fileManager.readCsv(filePath, charset = Charset.forName("UTF-8"))
        
        for ((index, row) in rows.withIndex()) {
            println("[INFO] Processing row ${index + 1} of ${rows.size}...")

            // CSV 행에서 필요한 값 추출 (파싱 실패 시 건너뜀)
            val parsed = parseCsvRow(row)
            if (parsed == null) {
                println("[WARN] Skipping invalid row - parsing failed: $row")
                continue
            }
            
             println("[INFO] Parsed data - Disease: ${parsed.diseaseName}, Category: ${parsed.categoryName}, Body System: ${parsed.bodySystemName}, Specialties: ${parsed.specialtyNames.joinToString()}")

            // 질병명이 이미 DB에 존재하는지 확인
            val existingDisease = diseaseRepository.findByName(parsed.diseaseName)

            // 존재하지 않으면 질병 엔티티 새로 생성 후 저장
            val disease = if (existingDisease == null) {
                println("[INFO] Saving new disease: ${parsed.diseaseName}")
                diseaseRepository.save(Disease(name = parsed.diseaseName))
            } else {
                println("[INFO] Disease already exists - skipping creation: ${parsed.diseaseName}")
                existingDisease
            }

            // 질병-분류 관계 저장
            println("[INFO] Saving disease-category relation")
            saveDiseaseCategory(disease, parsed.categoryName)
            
            // 질병-신체계통 관계 저장
            println("[INFO] Saving disease-body system relation")
            saveDiseaseBodySystem(disease, parsed.bodySystemName)

            // 질병-진료과 관계 저장
            println("[INFO] Saving disease-specialty relations")
            saveDiseaseSpecialties(disease, parsed.specialtyNames)
        }

        println("[INFO] Disease data import from CSV completed.")
    }

    // 전체 질병 목록을 페이지네이션하여 조회
    fun getAllDiseases(
        pageable: Pageable // 페이지 요청 정보 
    ): Page<Disease> { // 페이지 형태의 질병 목록
        return diseaseRepository.findAll(pageable)
    }

    // 질병명을 기준으로 질병 목록을 검색 (부분 일치)
    fun searchByName(
        name: String, // 검색할 질병명 키워드
        pageable: Pageable // 페이지 요청 정보 
    ): Page<Disease> { // 검색된 질병 목록
        return diseaseRepository.findByNameContainingIgnoreCase(name, pageable)
    }

    // 분류명을 기준으로 질병 목록을 검색 
    fun searchByCategoryName(
        categoryName: String, // 검색할 분류명
        pageable: Pageable // 페이지 요청 정보
    ): Page<Disease> { // 검색된 질병 목록
        return diseaseRepository.findByDiseaseCategory_Category_Name(categoryName, pageable)
    }

    // 신체계통명을 기준으로 질병 목록을 검색
    fun searchByBodySystemName(
        bodySystemName: String, // 검색할 신체계통명
        pageable: Pageable // 페이지 요청 정보
    ): Page<Disease> { // 검색된 질병 목록
        return diseaseRepository.findByDiseaseBodySystem_BodySystem_Name(bodySystemName, pageable)
    }

    // 진료과명을 기준으로 질병 목록을 검색
    fun searchBySpecialtyName(
        specialtyName: String, // 검색할 진료과명
        pageable: Pageable // 페이지 요청 정보
    ): Page<Disease> { // 검색된 질병 목록
        return diseaseRepository.findByDiseaseSpecialties_Specialty_Name(specialtyName, pageable)
    }
}
