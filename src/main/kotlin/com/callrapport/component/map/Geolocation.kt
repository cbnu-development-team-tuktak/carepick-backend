package com.callrapport.component.map

// Spring Framework 관련 import 
import org.springframework.stereotype.Component // Spring의 컴포넌트로 등록하는 어노테이션
import org.springframework.web.reactive.function.client.WebClient // 비동기 HTTP 요청을 위한 WebClient

// Reactor 관련 import 
import reactor.core.publisher.Mono // Reactor 라이브러리의 Mono, 비동기 단일 데이터 처리

// Jackson (JSON 처리) 관련 import 
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper // JSON 문자열을 객체로 변환하는 Jackson ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue // JSON 데이터를 Kotlin 객체로 매핑하는 확장 함수

// 네이버 API 설정 관련 import 
import com.callrapport.config.NaverApiProperties // 네이버 API 설정 클래스를 주입하여 클라이언트 ID와 Secret 사용

@Component
class Geolocation(naverApiProperties: NaverApiProperties) {
    private val webClient: WebClient = WebClient.builder()
        .baseUrl("https://naveropenapi.apigw.ntruss.com") // 네이버 Geocoding API 기본 URL 설정
        .defaultHeader("X-NCP-APIGW-API-KEY-ID", naverApiProperties.clientId) // API 요청 시 필요한 Client ID 헤더 추가
        .defaultHeader("X-NCP-APIGW-API-KEY", naverApiProperties.secret) // API 요청 시 필요한 Client Secret 헤더 추가
        .build()

    // 주소를 입력받아 네이버 API로부터 Geocode 정보를 가져오기
    fun getGeocode(address: String): Mono<String> {
        return webClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/map-geocode/v2/geocode")
                    .queryParam("query", address)
                    .build()
            }
            .retrieve()
            .bodyToMono(String::class.java)
    }
}