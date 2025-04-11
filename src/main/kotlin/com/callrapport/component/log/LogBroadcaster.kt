// LogBroadcaster.kt
package com.callrapport.component.log

// 메시지 전송 관련 import
import org.springframework.messaging.simp.SimpMessagingTemplate // STOMP 기반 메시지를 특정 대상에게 전송하는 데 사용 

// Spring Framework 관련 import
import org.springframework.stereotype.Component // 해당 클래스의 Spring의 빈으로 등록

@Component
class LogBroadcaster(
    private val messagingTemplate: SimpMessagingTemplate // STOMP 메시지 전송을 위한 템플릿
) {
    // 로그 메시지를 전송
    fun sendLog(
        message: String // 전송할 메시지 내용
    ) {
        // 지정한 주제(/topic/logs)를 구독 중인 모든 클라이언트에게 메세지를 전송
        messagingTemplate.convertAndSend("/topic/logs", message)
    }
}
