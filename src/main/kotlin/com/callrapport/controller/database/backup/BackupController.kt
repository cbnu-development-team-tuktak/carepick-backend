package com.callrapport.controller.database

// Spring 관련 import
import org.springframework.web.bind.annotation.RestController // REST 컨트롤러 등록
import org.springframework.web.bind.annotation.RequestMapping // 공통 URL 매핑
import org.springframework.web.bind.annotation.GetMapping // GET 요청 매핑
import org.springframework.http.ResponseEntity // HTTP 응답 래퍼

// 서비스 관련 import
import com.callrapport.service.database.BackupService // 백업 서비스 클래스

@RestController
@RequestMapping("/api/backup")
class BackupController(
    private val backupService: BackupService // 백업 서비스 
) {
    // 백업된 .sql 파일 목록 조회
    // 예: http://localhost:8080/api/backup/files
    @GetMapping("/files")
    fun listBackupFiles(): ResponseEntity<List<String>> {
        return try {
            val files = backupService.listBackupFiles().map { it.name } 
            ResponseEntity.ok(files)
        } catch (e: Exception) {
            ResponseEntity.internalServerError().build()
        }
    }

    // 전체 데이터베이스 백업
    // 예: http://localhost:8080/api/backup/database
    @GetMapping("/database")
    fun backupDatabase(): ResponseEntity<String> {
        return try {
            val backupFile = backupService.backupDatabase() 
            ResponseEntity.ok("✅ 백업 성공: ${backupFile.name}") 
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body("❌ 백업 실패: ${e.message}") 
        }
    }
}
