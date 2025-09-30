package com.callrapport.dto.request

/**
 * ChatGPT API 요청을 위한 DTO (Data Transfer Object)
 *
 * @property model 사용할 AI 모델 (예: "gpt-3.5-turbo")
 * @property messages 전송할 메시지 목록
 */
data class ChatgptRequest(
    val model: String,
    val messages: List<Message>
)

/**
 * ChatGPT 메시지 구조를 나타내는 공용 DTO
 *
 * @property role 메시지 발신자 역할 ("user", "assistant", "system")
 * @property content 메시지 내용
 */
data class Message(
    val role: String,
    val content: String
)
