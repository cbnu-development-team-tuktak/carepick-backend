package com.callrapport.controller.test

// Spring 관련 import 
import org.springframework.web.bind.annotation.* // REST 컨트롤러, 매핑, 요청 파라미터 어노테이션 등
import org.springframework.http.ResponseEntity // HTTP 응답 클래스

// 키워드 추출 관련 import
import com.callrapport.component.keyword.KeywordExtractor

@RestController 
@RequestMapping("api/keywords")
class KeywordController (
    private val keywordExtractor: KeywordExtractor // 키워드 추출 컴포넌트
) {
    // 동의어 사전
    private val synonymDictionary = mapOf(
        "머리" to listOf("두통", "어지러움", "멀미")
    )

    // 동의어 교체
    private fun replaceWithSynonyms(
        text: String // 텍스트 내에서 동의어를 교체할 대상 텍스트
    ): String { // 동의어 교체 후 수정된 텍스트
        var updatedText = text // 입력할 텍스트를 처리할 변수로 복사

        // 동의어 사전에서 각 동의어 그룹을 순회
        synonymDictionary.forEach { (synonym, words) ->
            // 동의어 목록에 있는 각 단어를 동의어로 바꿈
            words.forEach { word ->
                // 텍스트 내에서 해당 단어를 동의어로 치환
                updatedText = updatedText.replace(word, synonym)
            }
        }
        return updatedText // 최종적으로 동의어로 교체된 텍스트를 반환
    }

    // 코사인 유사도 계산
    private fun cosineSimilarity(vec1: List<Double>, vec2: List<Double>): Double {
        // 두 벡터의 내적 계산 (각 대응되는 원소들의 곱을 합산)
        val dotProduct = vec1.zip(vec2).sumOf { it.first * it.second }

        // 첫 번째 벡터의 크기 계산 (벡터의 각 원소 제곱합의 제곱근)
        val magnitude1 = Math.sqrt(vec1.sumOf { it * it })
        
        // 두 번째 벡터의 크기 계산 (벡터의 각 원소 제곱합의 제곱근)
        val magnitude2 = Math.sqrt(vec2.sumOf { it * it })
        
        // 두 벡터의 크기가 0인 경우 유사도를 0으로 반환
        // 그렇지 않으면 코사인 유사도 계산 (내적 / 크기1 * 크기2)
        return if (magnitude1 == 0.0 || magnitude2 == 0.0) 0.0
               else dotProduct / (magnitude1 * magnitude2)
    }

    // 키워드 목록을 벡터로 변환
    private fun toVector(
        allKeywords: List<String>, // 전체 키워드 목록 (문서에서 나타날 수 있는 모든 키워드)
        docKeywords: List<String> // 특정 문서에서 추출한 키워드 목록
    ): List<Double> { // 각 키워드가 문서에 몇 번 나타나는지에 대한 벡터 목록
        return allKeywords.map { keyword ->
            // 각 키워드에 대해 문서에서 해당 키워드가 등장하는 횟수를 계산
            docKeywords.count { it == keyword }.toDouble()
        }
    }

    // 여러 문장과 비교 (TextRank 기반)
    // 예: http://localhost:8080/api/keywords/compare/multi/text_rank
    @GetMapping("/compare/multi/text_rank")
    fun compareMultiTextsByTextRank(): ResponseEntity<Map<String, Any>> {
        // 사용자가 입력한 문장 (예시)
        val inputText = "머리가 아프고, 호흡하기가 어려워. 가슴이 답답해." 
        // 입력 문장에 대해 동의어 교체 처리
        val updatedInputText = replaceWithSynonyms(inputText)
        
        // 여러 질병에 대한 문장들 (질병별 증상 예시)
        val diseaseTextList = listOf(
            // 고혈압 관련 증상 문장
            "고혈압은 두통, 가슴 두근거림, 호흡곤란을 유발하는 질병입니다.",
            // 골절 관련 증상 문장
            "골절은 통증, 부종, 변형을 유발하는 질병입니다.",
            // 구내염 관련 증상 문장
            "구내염은 발진, 통증, 궤양을 유발하는 질병입니다."
        )

        // 각 질병 문장과의 유사도 계산
        val similarities = diseaseTextList.mapIndexed { index, diseaseText ->
            // 질병 문장에 대해서도 동의어 교체 처리
            val updatedDiseaseText = replaceWithSynonyms(diseaseText)

            // 입력 테스트와 질병 문장의 키워드 추출 (TextRank)
            val keywords1 = keywordExtractor.extractByTextRank(inputText, topN = 30).toSet()
            val keywords2 = keywordExtractor.extractByTextRank(updatedDiseaseText, topN = 30).toSet()
            
            // 두 문장의 키워드를 합친 전체 키워드 목록
            val allKeywords = (keywords1 + keywords2).toSet().toList()

            // 두 문장의 키워드 벡터를 변환
            val vec1 = toVector(allKeywords, keywords1.toList())
            val vec2 = toVector(allKeywords, keywords2.toList())

            // Cosine 유사도를 계산하여 두 문장의 유사도 구하기
            val similarity = cosineSimilarity(vec1, vec2)

            // (문장 번호, 유사도) 튜플로 반환
            Pair(index + 1, similarity)
        }

        // 각 질병 문장과의 유사도 점수 및 키워드 목록를 반환
        return ResponseEntity.ok(
            mapOf(
                // 사용자가 입력한 문장
                "input_text" to inputText,
                // 각 질병 문장과의 유사도 점수 리스트
                "similarities" to similarities, 
                // 입력 문장의 키워드
                "input_text_keywords" to keywordExtractor.extractByTextRank(inputText, topN = 30), 
                // 각 질병 문장의 키워드
                "disease_keywords" to diseaseTextList.map { replaceWithSynonyms(it).let { keywordExtractor.extractByTextRank(it, topN = 30) } } 
            )
        )
    }
}
