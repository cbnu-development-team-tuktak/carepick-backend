package com.callrapport.component.file

// Spring Component 관련 import
import org.springframework.stereotype.Component // 컴포넌트로 등록하여 의존성 주입 가능하게 함

// Java I/O 관련 import
import java.io.File // 파일 객체를 사용하기 위한 클래스

// 문자 인코딩 관련 import
import java.nio.charset.Charset // 파일 인코딩을 설정하기 위한 클래스

@Component
class FileManager {
    // CSV 파일을 읽고 각 라인을 맵의 리스트 형태로 반환
    fun readCsv(
        filePath: String, // 읽을 CSV 파일 경로
        charset: Charset = Charset.forName("MS949") // 파일 인코딩 (한글이 깨지지 않는 MS949)
    ): List<Map<String, String>> {
        val file = File(filePath) // 파일 객체 생성
        
        // 파일이 존재하지 않으면 예외 발생
        require(file.exists()) { "not file found: $filePath" }

        val lines = file.readLines(charset) // 지정한 인코딩으로 파일의 모든 줄을 읽기
        if (lines.isEmpty()) return emptyList() // 내용이 없으면 빈 리스트 반환
 
        val headers = lines.first().split(",") // 첫 줄(헤더)을 ',' 기준으로 분리 → 키 목록

        // 두 번째 줄부터 각 라인을 Map 형태로 매핑
        return lines.drop(1).map { line ->
            headers.zip(line.split(",")) // 헤더와 각 값 쌍으로 묶기
                .associate { (header, value) -> 
                    header.trim() to value.trim()  // 양쪽 공백 제거 후 Map 생성
                }
        }
    }
}