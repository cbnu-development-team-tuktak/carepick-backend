// LogBroadcaster.kt
package com.callrapport.component.log

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class LogBroadcaster(
    private val messagingTemplate: SimpMessagingTemplate
) {
    fun sendLog(message: String) {
        messagingTemplate.convertAndSend("/topic/logs", message)
    }
}
