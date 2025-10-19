package com.callrapport.service.map
 
import com.callrapport.component.file.FileManager // 파일 관리 컴포넌트를 가져오기
import com.callrapport.model.administrativeRegion.* // JPA 엔티티들을 가져오기
import com.callrapport.repository.administrativeRegion.* // Spring Data JPA 리포지토리들을 가져오기
import org.springframework.stereotype.Service // Spring의 서비스 어노테이션을 가져오기
import org.springframework.transaction.annotation.Transactional // 트랜잭션 관리를 위한 어노테이션을 가져오기
import java.nio.charset.Charset // 문자 인코딩을 위한 패키지를 가져오기

// DTO 관련 import
import com.callrapport.dto.SidoDetailsResponse // 시도 상세 응답 DTO
import com.callrapport.dto.SggDetailsResponse // 시군구 상세 응답 DTO
import com.callrapport.dto.UmdDetailsResponse // 읍면동 상세 응답 DTO


// 페이지네이션 관련 import
import org.springframework.data.domain.Page // 페이징 결과를 담는 객체
import org.springframework.data.domain.Pageable // 페이징 요청 정보를 담는 객체

@Service
class AdministrativeRegionService(
    private val fileManager: FileManager, // 파일 관리 컴포넌트 주입
    private val sidoRepository: SidoRepository, // SidoRepository 주입
    private val sggRepository: SggRepository, // SggRepository 주입
    private val sggUmdRepository: SggUmdRepository, // SggUmdRepository 주입
    private val umdRepository: UmdRepository, // UmdRepository 주입
    private val sidoSggRepository: SidoSggRepository // SidoSggRepository 주입
) {
    fun clear() {
        // 모든 행정구역 데이터를 삭제
        sidoRepository.deleteAll() // 시도 데이터 삭제
        sggRepository.deleteAll() // 시군구 데이터 삭제
        umdRepository.deleteAll() // 읍면동 데이터 삭제
        sggUmdRepository.deleteAll() // 시군구-읍면동 관계 데이터 삭제
        sidoSggRepository.deleteAll() // 시도-시군구 관계 데이터 삭제
    }
    
    // 시도 목록을 CSV 파일에서 읽어와 데이터베이스에 저장하는 메서드
    fun saveSidoList(): List<Sido> {
        // 기존 시도 데이터를 모두 삭제
        sidoRepository.deleteAll()

        // CSV 파일에서 시도 목록을 읽어오기
        val rows = fileManager.readCsv("csv/sido_list.csv", Charset.forName("UTF-8"))

        // 읽어온 데이터를 Sido 엔티티로 변환하여 리스트로 생성
        val sidoList = rows.map {
            Sido(
                code = it["code"] ?: error("sido code missing"), // 시도 코드가 없으면 에러 발생
                name = it["name"] ?: error("sido name missing"), // 시도 이름이 없으면 에러 발생
                type = it["type"] ?: error("sido type missing") // 시도 타입이 없으면 에러 발생
            )
        }

        // 변환된 Sido 엔티티 리스트를 데이터베이스에 저장
        return sidoRepository.saveAll(sidoList)
    }

    // 시군구 목록을 CSV 파일에서 읽어와 데이터베이스에 저장하는 메서드
    fun saveSggList(): List<Sgg> {
        sidoSggRepository.deleteAll() // 기존 시도-시군구 관계 삭제
        sidoSggRepository.flush() // 즉시 반영

        sggRepository.deleteAll() // 기존 시군구 데이터 삭제
        sggRepository.flush() // 즉시 반영

        // CSV 파일에서 시도 목록을 읽어오기
        val sidoRows = fileManager.readCsv("csv/sido_list.csv", Charset.forName("UTF-8"))

        // 읽어온 데이터를 Sido 엔티티로 변환하여 리스트로 생성
        val sidoNameToCode = sidoRows.associate { it["name"]!! to it["code"]!! }

        // CSV 파일에서 시군구 목록을 읽어오기
        val sggRows = fileManager.readCsv("csv/sgg_list.csv", Charset.forName("UTF-8"))

        // 읽어온 데이터를 Sgg 엔티티로 변환하여 리스트로 생성
        val sggList = sggRows.map {
            Sgg(
                code = it["code"] ?: error("sgg code missing"), // 시군구 코드가 없으면 에러 발생
                name = it["name"] ?: error("sgg name missing"), // 시군구 이름이 없으면 에러 발생
                type = it["type"] ?: error("sgg type missing") // 시군구 타입이 없으면 에러 발생
            )
        }
        // 변환된 Sgg 엔티티 리스트를 데이터베이스에 저장
        val savedSggList = sggRepository.saveAll(sggList)

        // 시군구 이름 중복 인덱스 관리용 맵 생성
        val relations = sggRows.map { row ->
            val sggCode = row["code"] ?: error("sgg code missing in relation") // 시군구 코드가 없으면 에러 발생
            val parentName = row["parent_name"] ?: error("parent_name missing") // 부모 시도 이름이 없으면 에러 발생

            // 시도 이름으로 시도 엔티티 찾기
            val sido = sidoRepository.findByName(parentName) ?: error("Sido not found by name: $parentName")
            // 시군구 코드로 시군구 엔티티 찾기
            val sgg = sggRepository.findByCode(sggCode) ?: error("Sgg not found: $sggCode")

            // 시도와 시군구 관계를 나타내는 SidoSgg 엔티티 생성
            SidoSgg(sido = sido, sgg = sgg)
        }.distinctBy { it.sido.code to it.sgg.code } // 중복 관계 제거

        // 생성된 시도-시군구 관계를 데이터베이스에 저장
        sidoSggRepository.saveAll(relations)

        // 저장된 시군구 리스트 반환
        return savedSggList
    }

    // 읍면동 목록을 CSV 파일에서 읽어와 데이터베이스에 저장하는 메서드
    fun saveUmdList(): List<Umd> {
        // 1. 기존 관계 및 읍면동 데이터 삭제
        sggUmdRepository.deleteAll() // 기존 시군구-읍면동 관계 삭제
        sggUmdRepository.flush() // 즉시 반영
        umdRepository.deleteAll() // 기존 읍면동 데이터 삭제
        umdRepository.flush() // 즉시 반영

        // 2. CSV 읽기
        val rows = fileManager.readCsv("csv/umd_list.csv", Charset.forName("UTF-8"))

        // 3. Umd 리스트 생성 및 저장
        val umdList = rows.map {
            Umd(
                code = it["code"] ?: error("umd code missing"), // 읍면동 코드가 없으면 에러 발생
                name = it["name"] ?: error("umd name missing"), // 읍면동 이름이 없으면 에러 발생
                type = it["type"] ?: error("umd type missing") // 읍면동 타입이 없으면 에러 발생
            )
        }
        // 변환된 Umd 엔티티 리스트를 데이터베이스에 저장
        val savedUmdList = umdRepository.saveAll(umdList)

        // 4. 시군구 이름 중복 인덱스 관리용 맵 생성
        val sggNameCountMap = mutableMapOf<String, Int>()

        // 5. 관계 생성
        val relations = rows.map { row ->
            val umdCode = row["code"] ?: error("umd code missing in relation") // 읍면동 코드가 없으면 에러 발생
            val sggName = row["parent_name"] ?: error("parent_name missing") // 부모 시군구 이름이 없으면 에러 발생

            // 시군구 이름으로 시군구 엔티티 찾기
            val umd = umdRepository.findByCode(umdCode) ?: error("Umd not found: $umdCode")

            // 시군구 이름으로 여러개 찾기
            val sggCandidates = sggRepository.findAllByName(sggName)
            if (sggCandidates.isEmpty()) {
                error("Sgg not found by name: $sggName")
            }

            // 현재 시군구 이름 카운트(사용된 인덱스) 가져오기
            val count = sggNameCountMap.getOrDefault(sggName, 0)

            // 중복이 있으면 순차적으로 선택, 범위 넘어가면 0번째 선택
            val selectedSgg = if (count < sggCandidates.size) {
                sggCandidates[count]
            } else {
                sggCandidates[0]
            }

            // 카운트 증가시키기
            sggNameCountMap[sggName] = count + 1

            SggUmd(sgg = selectedSgg, umd = umd)
        }.distinctBy { it.sgg.code to it.umd.code } // 중복 관계 제거

        // 6. 관계 저장
        sggUmdRepository.saveAll(relations)

        // 7. 저장된 읍면동 리스트 반환
        return savedUmdList
    }

    // 전국 시/도 목록을 페이지 단위로 조회
    fun getSidoList(pageable: Pageable): Page<SidoDetailsResponse> {
        val sidos = sidoRepository.findAll(pageable)
        return sidos.map { SidoDetailsResponse.from(it) } // Sido는 부모가 없으니 parentCode 없음
    }

    // 전국 시/군/구 목록을 페이지 단위로 조회
    fun getSggList(pageable: Pageable): Page<SggDetailsResponse> {
        val sggs = sggRepository.findAll(pageable)
        return sggs.map { sgg -> 
            // 시군구 자체에서 부모 시도 코드 가져올 수 있다면 포함시키기
            val parentCode = sidoSggRepository.findBySgg(sgg).firstOrNull()?.sido?.code ?: ""
            SggDetailsResponse.from(sgg, parentCode = parentCode)
        }
    }

    // 전국 읍/면/동 목록을 페이지 단위로 조회
    fun getUmdList(pageable: Pageable): Page<UmdDetailsResponse> {
        val umds = umdRepository.findAll(pageable)
        return umds.map { umd ->
            val parentCode = sggUmdRepository.findByUmd(umd).firstOrNull()?.sgg?.code ?: ""
            UmdDetailsResponse.from(umd, parentCode = parentCode)
        }
    }

    // 특정 시/도에 속하는 시/군/구 목록을 페이지 단위로 조회 (부모 코드 포함)
    fun getSggListBySido(sidoName: String, pageable: Pageable): Page<SggDetailsResponse> {
        val sido = sidoRepository.findByName(sidoName)
            ?: throw IllegalArgumentException("Sido not found with name: $sidoName")
        val sidoSggList = sidoSggRepository.findBySido(sido, pageable)
        return sidoSggList.map { sidoSgg -> 
            SggDetailsResponse.from(sidoSgg.sgg, parentCode = sidoSgg.sido.code)
        }
    }

    // 특정 시/군/구에 속하는 읍/면/동 목록을 페이지 단위로 조회 (부모 코드 포함)
    fun getUmdListBySgg(sggName: String, pageable: Pageable): Page<UmdDetailsResponse> {
        val sgg = sggRepository.findByName(sggName)
            ?: throw IllegalArgumentException("Sgg not found with name: $sggName")
        val sggUmdList = sggUmdRepository.findBySgg(sgg, pageable)
        return sggUmdList.map { sggUmd ->
            UmdDetailsResponse.from(sggUmd.umd, parentCode = sggUmd.sgg.code)
        }
    }

    // 키워드로 시/군/구 목록을 조회 (부분 일치)
    @Transactional(readOnly = true)
    fun findSggsByKeyword(keyword: String): List<Sgg> {
        return sggRepository.findAllByNameContaining(keyword)
    }

    // 시/군/구로 시/도 조회
    @Transactional(readOnly = true)
    fun findSidoBySgg(sgg: Sgg): Sido? {
        return sidoSggRepository.findBySgg(sgg).firstOrNull()?.sido
    }

    // 키워드로 시/도 목록을 조회 (부분 일치)
    @Transactional(readOnly = true)
    fun findSidosByKeyword(keyword: String): List<Sido> {
        // Sido 엔티티에 대한 Repository가 필요합니다.
        // (예: sidoRepository.findAllByNameContaining(keyword))
        return sidoRepository.findAllByNameContaining(keyword) 
    }

    /**
     * 시/도 이름 키워드로 해당 시/도에 속한 모든 시/군/구(Sgg) 목록을 조회합니다.
     * * 1. 키워드에 해당하는 시/도(Sido) 엔티티들을 찾습니다.
     * 2. 각 시/도에 연결된 모든 시/군/구(Sgg)들을 가져옵니다.
     * 3. 중복을 제거하고 하나의 목록으로 반환합니다.
     */
    @Transactional(readOnly = true)
    fun findSggsBySidoKeyword(sidoKeyword: String): List<Sgg> {
        // 1. 키워드에 해당하는 모든 Sido를 찾습니다.
        val targetSidos = findSidosByKeyword(sidoKeyword)
        
        if (targetSidos.isEmpty()) {
            return emptyList()
        }

        val allSggs = mutableSetOf<Sgg>()

        // 2. 각 Sido에 연결된 모든 Sgg를 찾습니다.
        targetSidos.forEach { sido ->
            // SidoSggRepository를 사용하여 특정 Sido에 속한 모든 연결 엔티티를 조회
            val sidoSggs = sidoSggRepository.findBySido(sido)
            
            // 연결 엔티티에서 Sgg만 추출하여 목록에 추가 (Set을 사용하여 중복 자동 제거)
            sidoSggs.map { it.sgg }.toCollection(allSggs)
        }

        // 3. Set을 List로 변환하여 반환
        return allSggs.toList()
    }
}