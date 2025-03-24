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
    // 동의어 사전 (예시)
    private val synonymDictionary = mapOf(
        "두통" to "머리",
        "어지러움" to "머리",
        "멀미" to "머리",
        "고혈압" to "고혈압 증상",
        "호흡곤란" to "숨 가쁨",
        "두근거림" to "심장 두근거림"
    )

    // 동의어 교체 함수
    private fun replaceWithSynonyms(text: String): String {
        var updatedText = text
        synonymDictionary.forEach { (key, value) ->
            updatedText = updatedText.replace(key, value)
        }
        return updatedText
    }

    private fun cosineSimilarity(vec1: List<Double>, vec2: List<Double>): Double {
        val dotProduct = vec1.zip(vec2).sumOf { it.first * it.second }
        val magnitude1 = Math.sqrt(vec1.sumOf { it * it })
        val magnitude2 = Math.sqrt(vec2.sumOf { it * it })
    
        return if (magnitude1 == 0.0 || magnitude2 == 0.0) 0.0
               else dotProduct / (magnitude1 * magnitude2)
    }

    private fun toVector(allKeywords: List<String>, docKeywords: List<String>): List<Double> {
        return allKeywords.map { keyword ->
            docKeywords.count { it == keyword }.toDouble()
        }
    }

    // 여러 문장과 비교 (TextRank 기반)
    // 예: http://localhost:8080/api/keywords/compare/multi/text_rank
    @GetMapping("/compare/multi/text_rank")
    fun compareMultiTextsByTextRank(): ResponseEntity<Map<String, Any>> {
        val inputText = "머리가 아프고, 호흡하기가 어려워. 가슴이 답답해." // 사용자가 입력한 문장
        val updatedInputText = replaceWithSynonyms(inputText)
        // 여러 질병에 대한 문장들
        val diseaseTextList = listOf(
            // 고혈압
            "고혈압은 표적 장기 손상으로 이어질 수 있습니다. 두통과 두근거림이 발생할 수 있습니다.",
            // 골절
            """
                뼈가 부러지면 극심한 통증이 나타나며, 함께 나타나는 주요 증상은 다음과 같습니다.

                통증 및 압통: 골절 부위의 부기(종창), 근육 경련, 골막의 손상에 의해 통증이 발생하고, 골절 부위를 압박하거나 움직일 때 더욱 심해집니다.
                정상 기능의 상실: 골절된 부위가 비정상적으로 흔들리고 주위 관절이 아파서 움직이지 못합니다.
                골절로 인한 변형: 팔, 다리의 모양이 변합니다.
                부종: 체액과 혈액이 손상 부위로 스며들어 팔, 다리가 붓습니다.
                그 외에 감각 손상, 근육 경련, 마비 등이 올 수 있습니다.
            """,
            // 구내염
            """
                대부분의 원발성 감염에서는 증상이 없거나 매우 경미하여 병에 걸린지 모르고 지나가는 경우가 많고, 단 10% 정도에서 감염 후 약 1주일 정도의 잠복기를 걸쳐 전신쇠약이나 근육통 같은 증상이 나타납니다. 이어 1~3일 후에 피부와 점막에 소수포성 발진이 나타나는데, 구강 내의 혀, 입술, 잇몸, 볼점막과 구개에 1~2 mm 크기의 작은 물집이 발생하고 바로 터져서 얕고 통증을 동반한 작고 불규칙한 궤양을 형성합니다. 궤양은 노란빛을 띠는 회색의 위막으로 덮여 있고 가장자리로 홍반성 테두리를 보입니다. 대게 2주 내에 반흔 없이 치유됩니다.
            """
        )

        // 각 질병 문장과의 유사도 계산
        val similarities = diseaseTextList.mapIndexed { index, diseaseText ->
            // 동의어 교체 후 처리
            val updatedDiseaseText = replaceWithSynonyms(diseaseText)

            val keywords1 = keywordExtractor.extractByTextRank(inputText, topN = 30).toSet()
            val keywords2 = keywordExtractor.extractByTextRank(updatedDiseaseText, topN = 30).toSet()

            val allKeywords = (keywords1 + keywords2).toSet().toList()

            val vec1 = toVector(allKeywords, keywords1.toList())
            val vec2 = toVector(allKeywords, keywords2.toList())

            val similarity = cosineSimilarity(vec1, vec2)

            // (문장 번호, 유사도) 튜플로 반환
            Pair(index + 1, similarity)
        }

        // 결과 반환: 각 질병 문장과의 유사도 점수 및 키워드 목록
        return ResponseEntity.ok(
            mapOf(
                "input_text" to inputText,
                "similarities" to similarities, // 각 질병 문장과의 유사도
                "text1_keywords" to keywordExtractor.extractByTextRank(inputText, topN = 30), // 입력 문장의 키워드
                "disease_keywords" to diseaseTextList.map { replaceWithSynonyms(it).let { keywordExtractor.extractByTextRank(it, topN = 30) } } // 질병 문장들의 키워드
            )
        )
    }
}
