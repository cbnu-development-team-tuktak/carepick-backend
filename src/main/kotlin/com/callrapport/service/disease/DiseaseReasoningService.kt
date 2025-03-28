package com.callrapport.service

// Component 관련 import
import com.callrapport.component.chatgpt.ChatgptClient // ChatGPT 호출 컴포넌트

// Spring 관련 import
import org.springframework.stereotype.Service // 서비스 클래스 어노테이션

// WebClient 관련 import
import reactor.core.publisher.Mono // 비동기 응답 처리용 Mono

@Service
class DiseaseReasoningService(
    private val chatgptClient: ChatgptClient
) {
    companion object {
        // ChatGPT에게 증상 추출을 요청할 때 사용할 프롬프트 템플릿 (지시 + 규칙)
        // <문장> 아래에 실제 분석할 문장을 이어 붙여 사용
        private val SYMPTOM_EXTRACTION_PROMPT = """
            <지시>
            다음 문장에서 증상만 명사형으로 추출해서 [증상1, 증상2, ...] 형식으로 반환해줘.

            <규칙>
            출력: 리스트 형식 (예: ["두통", "어지러움"])
            증상: 명사형 표현 (예: 두통, 어지러움), 신체 반응/느낌 포함 (예: 피로감, 통증), 질병명 제외 (예: 천식, 부비동염), 해부학적 부위명 제외 (예: 코, 인두부)

            <문장>
        """.trimIndent() // 멀티라인 문자열에서 공통 들여쓰기를 제거하여 깔끔하게 문자열을 구성
    }

    fun testExtractSymptoms(): Mono<String> {
        val sentence = """
        감기의 주요 증상은 콧물, 코막힘, 재채기, 인후통, 기침입니다. 주로 코, 인두부, 인후부 등 상기도에 국한됩니다. 발열은 유아와 소아가 성인에 비해 더 흔하게 발생합니다.
        인후통, 권태감, 발열이 시작된 후 하루나 이틀이 지나면 콧물, 코막힘, 기침이 발생합니다. 인후부의 동통(쑤시고 아픔), 건조감, 이물감도 느낄 수 있습니다.
        증상은 시작된 후 2~3일까지 최고로 심해진 후 1주 정도가 지나면 대부분 사라집니다. 일부 환자에게서는 증상이 2주까지 지속되기도 합니다. 감기로 인해 인후부가 손상되기도 하는데, 특히 건조한 계절에 감기로 손상된 인후부가 정상으로 회복이 되지 않으면 기침, 가래, 후두부의 이물감이 3주 이상 지속되기도 합니다. 흡연자의 경우 기침이 좀 더 심하고 오래 지속됩니다. 비염이 있는 경우 후비루 증후군이 지속되는 경우가 있고, 부비동염, 천식과 유사한 증상을 보이기도 합니다.
        원인 바이러스가 같을지라도 나이에 따라 발생 질환에 다소 차이가 있습니다. 파라인플루엔자 바이러스와 호흡기 세포 융합 바이러스는 소아에서 바이러스 폐렴, 후두 크루프(상기도 막힘), 세기관지염을 일으키지만, 성인에서는 감기만을 일으킵니다.
        """.trimIndent()
        
        // 고정된 지시문 + 문장을 합쳐 최종 프롬프트 구성
        val prompt = SYMPTOM_EXTRACTION_PROMPT + sentence

        // GPT에게 프롬프트 전달 후 응답 반환
        return chatgptClient.askQuestion(prompt)
    }
}