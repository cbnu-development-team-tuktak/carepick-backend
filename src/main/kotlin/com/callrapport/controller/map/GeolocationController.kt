package com.callrapport.controller

// Spring Framework Web 관련 import
import org.springframework.web.bind.annotation.GetMapping // HTTP GET 요청을 매핑하는 어노테이션
import org.springframework.web.bind.annotation.RequestParam // 쿼리 파라미터를 매핑하는 어노테이션
import org.springframework.web.bind.annotation.RestController // REST API 컨트롤러를 정의하는 어노테이션

// Reactor 관련 import
import reactor.core.publisher.Mono // 비동기 단일 값(또는 에러)를 표혀하는 Reactor 타입

// 좌표 처리 관련 import
import org.locationtech.jts.geom.Coordinate // JTS 라이브러리의 좌표 객체 (경도/위도 표현용)

// Service 관련 import
import com.callrapport.service.map.GeolocationService // 좌표 변환 및 위치 관련 비즈니스 로직을 처리하는 서비스

@RestController
class GeolocationController(private val geolocationService: GeolocationService) {
    // 주소를 통해 경도, 위도를 반환하는 API
    @GetMapping("/api/geolocation")
    fun getCoordinates(@RequestParam address: String): Mono<Coordinate> {
        return geolocationService.getCoordinates(address)
    }    
}