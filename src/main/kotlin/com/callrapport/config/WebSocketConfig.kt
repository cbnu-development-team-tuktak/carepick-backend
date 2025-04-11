package com.callrapport.config

// Spring WebSocket 설정 관련 import
import org.springframework.context.annotation.Configuration // 설정 클래스로 등록하기 위한 어노테이션
import org.springframework.messaging.simp.config.MessageBrokerRegistry // 메시지 브로커 설정을 위한 객체
import org.springframework.web.socket.config.annotation.* // WebSocket 설정 관련 인터페이스 및 어노테이션

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    // STOMP WebSocket 엔드포인트 "/ws-logs" 등록 + SockJS fallback 설정
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws-logs") // WebSocket 연결 경로
            .setAllowedOriginPatterns("*") // 모든 Origin 허용 (CORS)
            .withSockJS() // SockJS를 통한 대체 연결 방식 지원
    }
    // 구독 경로와 메시지 발생 경로(prefix) 지정
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/topic") // 클라이언트가 구독할 수 있는 경로(prefix)
        registry.setApplicationDestinationPrefixes("/app") // 클라이언트가 서버로 메시지를 보낼 때 사용하는 prefix
    }
}
