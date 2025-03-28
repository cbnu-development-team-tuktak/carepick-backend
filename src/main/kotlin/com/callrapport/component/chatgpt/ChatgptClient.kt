package com.callrapport.component.chatgpt

// Config 관련 import
import com.callrapport.config.ChatgptApiProperties // ChatGPT API 설정 클래스

// Spring 관련 import
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component // 컴포넌트 클래스 어노테이션

// WebClient (비동기 HTTP 통신) 관련 import
import org.springframework.web.reactive.function.client.WebClient // WebClient 빌더 및 요청 처리
import reactor.core.publisher.Mono // 비동기 응답 처리용 Mono

// JSON 직렬화/비직렬화 관련 import
import com.fasterxml.jackson.annotation.JsonProperty // JSON 필드 매핑 어노테이션

@Component
class ChatgptClient(
    private val chatgptApiProperties: ChatgptApiProperties, // baseUrl, model, apiKey 등의 설정
) {
    private val webClient = WebClient.builder() 
        .baseUrl(chatgptApiProperties.baseUrl) // ChatGPT API Base URL 설정
        .defaultHeader("Authorization", "Bearer ${chatgptApiProperties.apiKey}") // apiKey 사용
        .build() // WebClient 인스턴스 생성

    // 클래스가 생성될 때 apiKey 로그 출력
    init {
        println("[DEBUG] ChatGPT API Key = '${chatgptApiProperties.apiKey}'")
    }

    // 설정된 ChatGPT API 키 반환
    fun getApiKey(): String {
        return chatgptApiProperties.apiKey
    }

    // 사용자의 질문을 ChatGPT에 전달하고 응답 반환
    fun askQuestion(prompt: String): Mono<String> { 
        val request = ChatgptRequest( // 요청 객체 생성
            model = chatgptApiProperties.model, // 사용할 모델 지정 (예: gpt-3.5-turbo)
            messages = listOf(ChatMessage("user", prompt)) // 메시지 리스트 구성 
        )
        
        return webClient.post() // POST 요청
            .uri("/v1/chat/completions") // 요청 URL (타이포 수정: /competitions → /completions)
            .bodyValue(request) // 요청 바디 설정
            .retrieve() // 응답 수신
            .bodyToMono(ChatGptResponse::class.java) // 응답을 ChatGptResponse로 매핑
            .map { it.choices.firstOrNull()?.message?.content ?: "no response from chatgpt"} // 응답 메시지 추출
    }

    // ChatGPT 요청 DTO
    data class ChatgptRequest( // ChatGPT에 보낼 요청 바디 모델
        val model: String, // 사용할 모델 이름
        val messages: List<ChatMessage> // 대화 메시지 리스트
    )

    // ChatGPT 메시지 객체
    data class ChatMessage(
        val role: String, // 메시지 역할 (user, system, assistant)
        val content: String // 사용자 질문 또는 응답 내용
    )

    // ChatGPT 응답 전체 구조
    data class ChatGptResponse(
        val choices: List<Choice> // 응답 선택지 리스트
    )

    // 각 선택지 안의 메시지
    data class Choice(
        val message: ChatMessage // 응답 메시지 객체
    )
}