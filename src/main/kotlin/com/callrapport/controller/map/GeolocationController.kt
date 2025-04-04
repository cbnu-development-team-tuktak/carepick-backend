package com.callrapport.controller

// Spring Framework Web 관련 import
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

// Reactor 관련 import
import reactor.core.publisher.Mono

// 좌표 처리 관련 import
import org.locationtech.jts.geom.Coordinate

// Service 관련 import
import com.callrapport.service.map.GeolocationService

@RestController
class GeolocationController(private val geolocationService: GeolocationService) {

    // 주소를 통해 경도, 위도를 반환하는 API
    @GetMapping("/api/geolocation")
    fun getCoordinates(@RequestParam address: String): Mono<Coordinate> {
        return geolocationService.getCoordinates(address)
    }
}