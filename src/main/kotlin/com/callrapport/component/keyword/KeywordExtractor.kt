package com.callrapport.component.keyword

// Spring 관련 import
import org.springframework.stereotype.Component // 스프링 컴포넌트로 등록하기 위한 어노테이션

// JgraphT 관련 import (그래프 구성 및 PageRank 알고리즘 사용을 위한 라이브러리)
import org.jgrapht.graph.DefaultWeightedEdge // 가중치 엣지를 표현하는 클래스
import org.jgrapht.graph.SimpleWeightedGraph // 단순 가중치 그래프를 생성하기 위한 클래스
import org.jgrapht.graph.PageRank // PageRank 알고리즘을 실행하기 위한 클래스

@Component
class KeywordExtractor {
    // TF-IDF 기반 키워드 추출
    fun extractByTfIdf(
        text: String, // 키워드를 추출할 대상 텍스트
        topN: Int = 10 // 최대 키워드 추출 개수
    ): List<String> { // 추출한 키워드 리스트

    }

    // TextRank 기반 키워드 추출
    fun extractByTextRank(
        text: String, // 키워드를 추출할 대상 텍스트
        topN: Int = 10 // 최대 키워드 추출 개수
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
                if (w1 != w2 && graphs.containsVertex(w1) && graphs.containsVertex(w2)) {
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
        topN: Int = 10 // 최대 키워드 추출 개수
        ): List<String> { // 추출한 키워드 리스트
    }
}