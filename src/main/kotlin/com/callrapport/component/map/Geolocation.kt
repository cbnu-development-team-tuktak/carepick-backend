package com.callrapport.component.map

// 네이버 지도 API 요청을 Spring WebClient 관련 라이브러리
import org.springframework.stereotype.Component // Spring의 컴포넌트로 등록하는 어노테이션
import org.springframework.web.reactive.function.client.WebClient // 비동기 HTTP 요청을 위한 WebClient
import org.springframework.beans.factory.annotation.Value // application.properties에서 설정 값을 주입받는 어노테이션
import reactor.core.publisher.Mono // Reactor 라이브러리의 Mono, 비동기 단일 데이터 처리

// JSON 응답을 처리하기 위한 Jackson 라이브러리
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper // JSON 문자열을 객체로 변환하는 Jackson ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue // JSON 데이터를 Kotlin 객체로 매핑하는 확장 함수

@Component
class Geolocation {
    @Value("\${naver.api.client-id}")
    private lateinit var clientId: String // 네이버 API 클라이언트 ID

    @Value("\${naver.api.client-secret}")
    private lateinit var clientSecret: String // 네이버 API 클라이언트 Secret 키

    private val webClient: WebClient = WebClient.builder()
        .baseUrl() // 네이버 Geocoding API 기본 URL 설정
        .defaultHeader() // API 요청 시 필요한 Client ID 헤더 추가
        .defaultHeader() // API 요청 시 필요한 Client Secret 헤더 추가
        .build() 
}