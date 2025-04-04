package com.callrapport.service.map

// Spring Framework 관련 import
import org.springframework.stereotype.Service // Service 어노테이션으로 등록

// Reactor 관련 import
import reactor.core.publisher.Mono

// Component 관련 import
import com.callrapport.component.map.Geolocation // Geolocation 컴포넌트 주입

// JSON 파싱 관련 import
import com.fasterxml.jackson.databind.ObjectMapper

// 좌표 처리 관련 import
import org.locationtech.jts.geom.Coordinate

@Service
class GeolocationService(private val geolocation: Geolocation) {

    private val objectMapper = ObjectMapper()

    // 주소를 통해 경도, 위도를 Coordinate 형태로 반환하는 메서드
    fun getCoordinates(address: String): Mono<Coordinate> {
        return geolocation.getGeocode(address)
            .map { jsonString ->
                val jsonNode = objectMapper.readTree(jsonString)
                val addresses = jsonNode["addresses"]
                if (addresses != null && addresses.isArray && addresses.size() > 0) {
                    val firstResult = addresses[0]
                    val latitude = firstResult["y"].asDouble()
                    val longitude = firstResult["x"].asDouble()
                    Coordinate(longitude, latitude)
                } else {
                    throw IllegalStateException("No coordinates found for the given address")
                }
            }
    }
}