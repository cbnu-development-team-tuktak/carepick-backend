package com.callrapport.component.keyword

// Spring 관련 import
import org.springframework.stereotype.Component // 스프링 컴포넌트로 등록하기 위한 어노테이션

// JgraphT 관련 import (그래프 구성 및 PageRank 알고리즘 사용을 위한 라이브러리)
import org.jgrapht.graph.DefaultWeightedEdge // 가중치 엣지를 표현하는 클래스
import org.jgrapht.graph.SimpleWeightedGraph // 단순 가중치 그래프를 생성하기 위한 클래스
import org.jgrapht.alg.scoring.PageRank // PageRank 알고리즘을 실행하기 위한 클래스

import org.openkoreantext.processor.OpenKoreanTextProcessorJava // OpenKoreanText 형태소 분석기 핵심 클래스 (정규화, 토큰화, 품사 분석 등 수행)
import scala.collection.JavaConverters // Scala의 컬렉션을 Java/Kotlin 컬렉션으로 변환하기 위한 유틸리티 (토큰 리스트 처리 시 필요)

// 불용어 관련 import
import com.callrapport.repository.common.StopwordRepository // 불용어 레포지토리

@Component
class KeywordExtractor(
    private val stopwordRepository: StopwordRepository // 불용어 레포지토리
) {
    // 입력된 텍스트를 전처리하여 의미 있는 단어만 추출
    private fun preprocessText(
        text: String // 전처리를 수행할 텍스트
    ): List<String> { // 추출한 의미 있는 단어 목록
        // DB에서 모든 불용어 단어 가져오기
        val stopwordSet = stopwordRepository.findAll().map { it.word }.toSet()

        // 정규화
        val normalized = OpenKoreanTextProcessorJava.normalize(text)

        // 토큰화
        val tokens = OpenKoreanTextProcessorJava.tokenize(normalized)

        // 품사 분석 + 명사/형용사/동사 추출
        val tokenList = OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens)
        val filtered = tokenList
            // 품사가 명사(Noun), 형용사(Adjective), 동사(Verb)인 경우만 필터링
             .filter { it.pos.toString() in listOf("Noun") }
            
            // 필터링된 형태소에서 실제 텍스트(단어)만 추출
            .map { it.text }

            // 추출된 단어 중 길이가 2자 이상이고, 불용어 목록에 없는 단어만 남김
            .filter { it.length > 1 && it !in stopwordSet }
    
        return filtered
    }

    // TF-IDF 기반 키워드 추출
    fun extractByTfIdf(
        text: String, // 키워드를 추출할 대상 텍스트
        topN: Int = 10 // 최대 키워드 추출 개수
    ): List<String> { // 추출한 키워드 리스트
        val words = preprocessText(text)
        if (words.isEmpty()) return emptyList()
    
        val wordFreq = words.groupingBy { it }.eachCount()
        val totalWords = words.size.toDouble()
    
        // 단순 TF 점수 계산
        val tfMap = wordFreq.mapValues { it.value / totalWords }
    
        // IDF = log(1 + 전체 문서 수 / 해당 단어가 등장한 문서 수)
        // 여기선 문서가 1개라서 모든 단어에 동일하게 적용 (log(1 + 1/1) = log(2))
        val idf = Math.log(2.0)
    
        // TF-IDF 점수 계산
        val tfidf = tfMap.mapValues { it.value * idf }
    
        return tfidf.entries
            .sortedByDescending { it.value } // TF-IDF 점수 기준 내림차순 정렬
            .take(topN) // 상위 N개 추출
            .map { it.key } // 단어 리스트 반환
    }

    // TextRank 기반 키워드 추출
    fun extractByTextRank(
        text: String, // 키워드를 추출할 대상 텍스트
        topN: Int = 5 // 최대 키워드 추출 개수
        ): List<String> { // 추출한 키워드 리스트

        // 1. 텍스트 전처리 및 의미 있는 단어 추출 (명사 중심)
        val words = preprocessText(text)
        if (words.isEmpty()) return emptyList()

        // 2. 단어 그래프 구성 (단어 = 노드, 공동 등장 관계 = 엣지)
        val graph = SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge::class.java)

        // 단어를 노드로 추가
        val wordSet = words.toSet()
        wordSet.forEach { graph.addVertex(it) }

        // 단어 간 엣지 추가 (공동 등장한 단어쌍)
        for (i in 0 until words.size) { // 첫 번째 단어 인덱스
            for (j in i + 1 until words.size) { // 두 번째 단어 인덱스
                val w1 = words[i] // 첫 번째 단어
                val w2 = words[j] // 두 번째 단어

                // 단어가 서로 다르고, 그래프에 모두 존재할 경우
                if (w1 != w2 && graph.containsVertex(w1) && graph.containsVertex(w2)) {
                    // 두 단어 간의 엣지를 가져오고, 없으면 새로 추가
                    val edge = graph.getEdge(w1, w2) ?: graph.addEdge(w1, w2)

                    // 현재 엣지 가중치를 가져온 후 1만큼 증가
                    val currentWeight = graph.getEdgeWeight(edge)
                    graph.setEdgeWeight(edge, currentWeight + 1.0)
                }
            }
        }

        // 3. PageRank 알고리즘을 적용하여 단어 중요도 계산
        val pageRank = PageRank(graph)

        // 4. 중요도 기준으로 정렬 후 상위 키워드 반환
        return pageRank.scores // PageRank 결과: 단어 → 중요도 점수 맵
            .entries // Map을 엔트리(키-값 쌍)의 리스트로 변환
            .sortedByDescending { it.value } // 중요도(PgaeRank 점수) 기준으로 내림차순 정렬
            .take(topN) // 상위 topN개의 단어만 추출
            .map { it.key } // 단어(key)만 리스트로 변환하여 반환
    }

    // 의미 분석론 기반 키워드 추출
    fun extractBySemanticAnalysis(
        text: String, // 키워드를 추출할 대상 텍스트
        topN: Int = 5 // 최대 키워드 추출 개수
        ): List<String> { // 추출한 키워드 리스트
            return emptyList()
    }
}