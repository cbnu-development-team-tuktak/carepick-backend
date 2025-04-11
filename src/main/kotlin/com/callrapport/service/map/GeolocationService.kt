package com.callrapport.service.map

// Spring Framework 관련 import
import org.springframework.stereotype.Service // 해당 클래스를 서비스 빈으로 등록하기 위한 어노테이션

// Reactor 관련 import
import reactor.core.publisher.Mono // 비동기 단일 결과를 처리하는 리액티브 타입

// Component 관련 import
import com.callrapport.component.map.Geolocation // 위치 좌표 계산을 담당하는 Geolocation 컴포넌트

// JSON 파싱 관련 import
import com.fasterxml.jackson.databind.ObjectMapper // JSON 문자열을 객체로 매핑하거나 객체를 JSON으로 변환하는 Jackson 유틸 클래스

// 좌표 처리 관련 import
import org.locationtech.jts.geom.Coordinate // 위도와 경도를 표현하는 좌표 객체 (JTS 라이브러리 사용)

@Service
class GeolocationService(
    private val geolocation: Geolocation // 위치 좌표 조회를 담당하는 Geolocation 컴포넌트
) {
    // JSON 문자열을 파싱하기 위한 Jackson의 ObjectMapper
    private val objectMapper = ObjectMapper()

    // 주소를 통해 경도, 위도를 Coordinate 형태로 반환하는 메서드
    fun getCoordinates(address: String): Mono<Coordinate> {
        return geolocation.getGeocode(address) // 비동기로 주소에 대한 좌표 정보를 조회
            .map { jsonString ->
                val jsonNode = objectMapper.readTree(jsonString) // JSON 문자열을 트리 구조로 파싱
                val addresses = jsonNode["addresses"] // "addresses" 배열 노드를 가져옴

                // 주소 정보가 존재하고 배열이 비어 있지 않은 경우
                if (addresses != null && addresses.isArray && addresses.size() > 0) {
                    val firstResult = addresses[0] // 첫 번째 주소 결과를 선택
                    val latitude = firstResult["y"].asDouble() // 위도(y) 추출
                    val longitude = firstResult["x"].asDouble() // 경도(x) 추출
                    Coordinate(longitude, latitude) // Coordinate 객체로 변환하여 반환
                } else {
                    // 좌표 정보를 찾을 수 없는 경우 예외 발생
                    throw IllegalStateException("No coordinates found for the given address")
                }
            }
    }
}