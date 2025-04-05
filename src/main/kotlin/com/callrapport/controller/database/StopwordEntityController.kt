package com.callrapport.controller.common

// Spring 관련 import 
import org.springframework.web.bind.annotation.* // REST 컨트롤러, 매핑, 요청 파라미터 어노테이션 등
import org.springframework.http.ResponseEntity // HTTP 응답을 표현하는 클래스  

// 저장소 관련 import
import com.callrapport.repository.common.StopwordRepository // 불용어(stopword) 데이터를 관리하는 저장소

// 엔티티 관련 import
import com.callrapport.model.common.Stopword // 불용어(stopword) 엔티티 클래스

@RestController
@RequestMapping("/api/stopwords")
class StopwordEntityController(
    private val stopwordRepository: StopwordRepository // 불용어 레포지토리 주입
) {
    // 초기 불용어 데이터를 저장
    // 예: http://localhost:8080/api/stopwords/initialize
    @GetMapping("/initialize")
    fun initializeStopwords(): ResponseEntity<Map<String, Any>> {
        val stopwords = listOf(
            // 일반 연결어 및 설명어
            Stopword(word = "은", pos = "조사"),
            Stopword(word = "는", pos = "조사"),
            Stopword(word = "이", pos = "조사"),
            Stopword(word = "가", pos = "조사"),
            Stopword(word = "을", pos = "조사"),
            Stopword(word = "를", pos = "조사"),
            Stopword(word = "에", pos = "조사"),
            Stopword(word = "도", pos = "조사"),
            Stopword(word = "와", pos = "조사"),
            Stopword(word = "과", pos = "조사"),
            Stopword(word = "등", pos = "명사"),
            Stopword(word = "이지만", pos = "접속사"),
            Stopword(word = "그러나", pos = "접속사"),
            Stopword(word = "그리고", pos = "접속사"),
            Stopword(word = "또는", pos = "접속사"),
            Stopword(word = "혹은", pos = "접속사"),
            Stopword(word = "수", pos = "의존명사"),
            Stopword(word = "것", pos = "의존명사"),
            Stopword(word = "경우", pos = "명사"),
            Stopword(word = "때", pos = "명사"),
            Stopword(word = "등의", pos = "명사"),
            Stopword(word = "등이", pos = "명사"),
            Stopword(word = "등등", pos = "명사"),
            Stopword(word = "이러한", pos = "관형사"),
            Stopword(word = "이것", pos = "대명사"),
            Stopword(word = "그것", pos = "대명사"),
            Stopword(word = "저것", pos = "대명사"),
            
            // 의학적 설명어 및 문맥어
            Stopword(word = "증상", pos = "명사"),
            Stopword(word = "증세", pos = "명사"),
            Stopword(word = "증후", pos = "명사"),
            Stopword(word = "손상", pos = "명사"),
            Stopword(word = "이상", pos = "명사"),
            Stopword(word = "변화", pos = "명사"),
            Stopword(word = "상태", pos = "명사"),
            Stopword(word = "질환", pos = "명사"),
            Stopword(word = "질병", pos = "명사"),
            Stopword(word = "병", pos = "명사"),
            Stopword(word = "병명", pos = "명사"),
            Stopword(word = "나타나다", pos = "동사"),
            Stopword(word = "보이다", pos = "동사"),
            Stopword(word = "발생하다", pos = "동사"),
            Stopword(word = "생기다", pos = "동사"),
            Stopword(word = "확인되다", pos = "동사"),
            Stopword(word = "가능하다", pos = "형용사"),
            Stopword(word = "있습니다", pos = "동사"),
            Stopword(word = "있다", pos = "동사"),        
            Stopword(word = "없다", pos = "동사"),
            Stopword(word = "나타나다", pos = "동사"),
            Stopword(word = "나타날", pos = "동사"),
            Stopword(word = "나타난다", pos = "동사"),
            Stopword(word = "곤란", pos = "명사"),        
            Stopword(word = "장기", pos = "명사"),
            Stopword(word = "중요", pos = "형용사"),
            Stopword(word = "가능하다", pos = "형용사"),

            // 불필요한 수식어
            Stopword(word = "보통", pos = "부사"),
            Stopword(word = "일반적으로", pos = "부사"),
            Stopword(word = "흔히", pos = "부사"),
            Stopword(word = "종종", pos = "부사"),
            Stopword(word = "간혹", pos = "부사"),
            Stopword(word = "매우", pos = "부사"),
            Stopword(word = "중요", pos = "형용사"),
            Stopword(word = "특히", pos = "부사"),
            Stopword(word = "특정", pos = "관형사"),
            Stopword(word = "전체", pos = "명사"),
            Stopword(word = "일부", pos = "명사"),
            Stopword(word = "심각한", pos = "형용사"),
            Stopword(word = "경미한", pos = "형용사"),
            Stopword(word = "다양한", pos = "관형사"),
            Stopword(word = "여러", pos = "관형사"),
            Stopword(word = "앞서", pos = "부사"),
            Stopword(word = "이후", pos = "부사"),
            Stopword(word = "전까지", pos = "부사"),
            Stopword(word = "다음과", pos = "부사"),
            Stopword(word = "같은", pos = "관형사"),
        )


        // 기존 DB에 해당 단어(word)가 존재하지 않는 경우만 필터링하여 리스트로 반환
        val newStopwords = stopwords.filterNot { stopwordRepository.existsByWord(it.word) }
        // 필터링된 새로운 불용어들을 한 번에 DB에 저장
        stopwordRepository.saveAll(newStopwords)  

        // 성공 응답 반환
        return ResponseEntity.ok(
            mapOf(
                "status" to "Stopwords initialized successfully", // 상태 메시지
                "totalSaved" to newStopwords.size // 새로 저장한 불용어 수
            )
        )
    }

    // 전체 불용어 목록 조회
    // 예: http://localhost:8080/api/stopwords
    @GetMapping
    fun getAllStopwords(): ResponseEntity<List<Stopword>> {
        // DB에 저장된 모든 불용어 목록 조회 및 반환
        return ResponseEntity.ok(stopwordRepository.findAll())
    }
}