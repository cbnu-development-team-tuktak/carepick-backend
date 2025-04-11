package com.callrapport.controller.ping

// Spring Framework Web 관련 import
import org.springframework.http.ResponseEntity // HTTP 응답 본문과 상태 코드를 포함하는 객체
import org.springframework.web.bind.annotation.GetMapping // HTTP GET 요청을 매핑하는 어노테이션
import org.springframework.web.bind.annotation.RequestMapping // 클래스 레벨에서 기본 요청 경로를 지정하는 어노테이션
import org.springframework.web.bind.annotation.RestController // REST API 컨트롤러임을 나타내는 어노테이션

// 컴포넌트 관련 import
import com.callrapport.component.log.LogBroadcaster // WebSocket을 통해 로그 메시지를 실시간 전송하는 컴포넌트

@RestController
@RequestMapping("/api/log") 
class LogController(
    private val logBroadcaster: LogBroadcaster // 로그 브로드캐스트를 담당하는 컴포넌트
) {
    // 서버 연결 확인
    // 예: http://localhost:8080/api/log/ping
    @GetMapping("/ping")
    fun ping(): ResponseEntity<String> {
        return ResponseEntity.ok("pong") 
    }

    // 테스트 로그 전송
    // 예: http://localhost:8080/api/log/test
    @GetMapping("/test")
    fun sendSampleLog(): ResponseEntity<String> {
        // 현재 시간 기반의 샘플 로그 메시지 생성
        val sampleLog = "[테스트 로그] ${System.currentTimeMillis()} 에 로그 송신됨"
        
        // 로그 브로드캐스터를 통해 WebSocket으로 로그 전송
        logBroadcaster.sendLog(sampleLog)

        // 전송한 로그 메시지를 HTTP 200 OK 상태로 변환
        return ResponseEntity.ok(sampleLog)
    }
}
