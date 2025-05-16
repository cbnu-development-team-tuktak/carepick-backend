package com.callrapport.service.map

// Component 관련 import
import com.callrapport.component.file.FileManager // CSV 파일 읽기 컴포넌트

// Model (엔티티) 관련 import
import com.callrapport.model.common.AdministrativeRegion // 행정구역 엔티티

// Repository (저장소) 관련 import
import com.callrapport.repository.common.AdministrativeRegionRepository // 행정구역 리포지토리

// Spring 관련 import
import org.springframework.data.domain.Page // 페이징된 응답 객체 (조회 결과를 Page<T> 형태로 반환할 때 사용)
import org.springframework.data.domain.Pageable // 페이지 요청 정보 (page 번호, size, 정렬 조건 등)
import org.springframework.stereotype.Service // 서비스 계층 클래스에 사용하는 어노테이션 (스프링 빈으로 등록됨)
import org.springframework.transaction.annotation.Transactional // 트랜잭션 처리를 위한 어노테이션

@Service
class AdministrativeRegionService(
    private val administrativeRegionRepository: AdministrativeRegionRepository, // 행정구역 리포지토리
    private val fileManager: FileManager // CSV 파일 읽기 컴포넌트
) {
    // CSV 파일로부터 행정구역 데이터를 읽고 DB에 저장
    @Transactional
    fun saveAdministrativeRegion() {
        val filePath = "csv/LSCT_LAWDCD.csv" // 읽어올 CSV 파일 경로
        val regions = fileManager.readCsv(filePath) // CSV 파일을 읽어 Map 리스트로 변환

        // CSV에서 읽은 데이터를 엔티티 리스트로 변환
        val entities = regions.map {
            AdministrativeRegion(
                sidoNm = it["SIDO_NM"] ?: "", // 시도명
                sggNm = it["SGG_NM"] ?: "", // 시군구명
                umdNm = it["UMD_NM"]?.takeIf { umd -> umd.isNotBlank() }, // 읍면동명
                riNm = it["RI_NM"]?.takeIf { ri -> ri.isNotBlank() } // 리명
            )
        }

        // 엔티티 리스트를 DB에 일괄 저장
        administrativeRegionRepository.saveAll(entities)
    }

    // 전체 행정구역 조회
    fun findAll(pageable: Pageable): Page<AdministrativeRegion> {
        return administrativeRegionRepository.findAll(pageable)
    }

    // 시도명으로 행정구역 조회
    fun findBySido(sido: String, pageable: Pageable): Page<AdministrativeRegion> {
        return administrativeRegionRepository.findBySidoNm(sido, pageable)
    }

    // 시군구명으로 행정구역 조회
    fun findBySgg(sgg: String, pageable: Pageable): Page<AdministrativeRegion> {
        return administrativeRegionRepository.findBySggNm(sgg, pageable)
    }

    // 읍면동명으로 행정구역 조회
    fun findByUmd(umd: String, pageable: Pageable): Page<AdministrativeRegion> {
        return administrativeRegionRepository.findByUmdNm(umd, pageable)
    }

    // 리명으로 행정구역 조회
    fun findByRi(ri: String, pageable: Pageable): Page<AdministrativeRegion> {
        return administrativeRegionRepository.findByRiNm(ri, pageable)
    }

    // 시도명만 반환
    fun findSidoNames(): List<String> {
        return administrativeRegionRepository.findDistinctSidoNames()
    }

    // 시도명으로 읍면동 목록 조회
    fun findUmdBySido(sido: String, pageable: Pageable): List<String> {
        // 시도에 해당하는 읍면동 목록만 추출 (중복 제거)
        val regions = administrativeRegionRepository.findBySidoNm(sido, pageable)
        return regions.mapNotNull { it.umdNm }.distinct() // 읍면동명만 추출하고 중복 제거
    }

    // 모든 행정구역 데이터 삭제
    @Transactional
    fun deleteAll() {
        administrativeRegionRepository.deleteAll()
    }
}