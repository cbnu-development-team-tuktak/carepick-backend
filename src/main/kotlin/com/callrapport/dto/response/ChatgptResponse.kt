package com.callrapport.dto.response

import com.callrapport.dto.request.Message

/**
 * ChatGPT API 응답을 위한 DTO
 *
 * @property choices API가 생성한 응답 선택지 목록
 */
data class ChatgptResponse(
    val choices: List<Choice>
)

/**
 * ChatGPT 응답 선택지를 나타내는 DTO
 *
 * @property message 응답으로 받은 실제 메시지 객체
 */
data class Choice(
    val message: Message
)
