package com.callrapport

// Spring Boot 애플리케이션 관련
import org.springframework.boot.autoconfigure.SpringBootApplication // Spring Boot의 자동 설정을 활성화하는 어노테이션
import org.springframework.boot.runApplication // 애플리케이션을 실행하는 함수

@SpringBootApplication
class CallRapportApplication

fun main(args: Array<String>) {
	// Spring Boot 애플리케이션 실행
	runApplication<CallRapportApplication>(*args)
}
