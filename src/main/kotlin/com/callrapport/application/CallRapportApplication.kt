package com.callrapport

// Spring Boot 애플리케이션 관련 import
import org.springframework.boot.autoconfigure.SpringBootApplication // Spring Boot의 자동 설정을 활성화하는 어노테이션
import org.springframework.boot.runApplication // 애플리케이션을 실행하는 함수
import org.springframework.boot.context.properties.EnableConfigurationProperties // 설정 프로퍼티 클래스 활성화 어노테이션

// 사용자 정의 설정 프로퍼티 클래스 import
import com.callrapport.config.NaverApiProperties // 네이버 API 설정을 담고 있는 프로퍼티 클래스

// Spring Boot의 자동 설정 활성화 및 프로퍼티 클래스 활성화
@SpringBootApplication
@EnableConfigurationProperties(
    value = [
        NaverApiProperties::class, // NaverApiProperties 클래스 활성화
    ] 
)
class CallRapportApplication
fun main(args: Array<String>) {
    // Spring Boot 애플리케이션 실행
    runApplication<CallRapportApplication>(*args)
}