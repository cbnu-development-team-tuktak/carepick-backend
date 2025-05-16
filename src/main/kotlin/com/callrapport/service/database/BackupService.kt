package com.callrapport.service.database

// Spring 서비스 관련 import
import org.springframework.stereotype.Service // 해당 클래스를 Spring의 서비스 컴포넌트로 등록하는 어노테이션

// 파일 처리 및 날짜 포맷 관련 import 
import java.io.File // 파일 생성 및 경로 지정에 사용
import java.time.LocalDateTime // 현재 시간 정보 제공
import java.time.format.DateTimeFormatter // 날짜 및 시간 포맷 지정 도구

@Service
class BackupService {
    // 백업 파일이 저장될 디렉터리 경로 (상대 경로 기준)
    private val backupDir = "backup/"

    fun listBackupFiles(): List<File> {
        val dir = File(backupDir)

        if (!dir.exists() || !dir.isDirectory) {
            return emptyList()
        }

        return dir.listFiles { file -> 
            file.isFile && file.extension == "sql" 
        }?.toList() ?: emptyList()
    }
   
    // 전체 데이터베이스를 백업
    fun backupDatabase(): File {
        // 현재 시간을 yyyyMMdd_HHmmss 포맷으로 변환하여 타임스탬프 생성
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))

        // 백업 파일 이름 구성 (예: backup_20250411_144210.sql)
        val filename = "backup_$timestamp.sql"

        // 디렉토리 경로와 파일 이름을 합쳐 전체 백업 파일 경로 생성
        val filePath = "$backupDir$filename"

        val command = listOf(
            "mysqldump", // MySQL 데이터베이스를 덤프(백업)하기 위한 명령어
            "-h", "carepick.cxu4scag8p2u.ap-northeast-2.rds.amazonaws.com", // RDS 호스트 주소
            "-P", "3306", // 포트 번호 (기본값 3306)
            "-u", "admin", // MySQL 사용자 이름 (admin 계정 사용)
            "--password=1q2w3e4r!", // MySQL 비밀번호를 포함한 로그인 정보
            "carepick", // 백업 대상이 되는 데이터베이스 이름
            "--result-file=$filePath" // 백업 파일을 저장할 경로 지정 
        )

        // 백업 명령어(command)를 실행할 프로세스를 생성하고 실행
        val process = ProcessBuilder(command)
            .redirectErrorStream(true) // 오류 스트림을 표준 출력 스트림으로 병합하여 출력
            .start() // 프로세스 시작 (mysqldump 실행)
        
        // 프로세스의 출력(표준 출력 및 에러)을 읽어 로그로 출력
        val output = process.inputStream.bufferedReader().readText()
        println("🪵 mysqldump output log:\n$output")
        // 명령어 실행이 완료될 때까지 대기하고, 종료 코드를 반환받음
        val exitCode = process.waitFor()

        // 종료 코드가 0이 아니면 백업에 실패한 것으로 간주하고 예외 발생
        if (exitCode != 0) {
            throw RuntimeException("데이터베이스 백업 실패: exitCode=$exitCode")
        }

        // 백업된 파일 객체를 반환
        return File(filePath)
    }

    fun backupTable(tableName: String): File {
        throw NotImplementedError("테이블 백업 기능은 아직 구현되지 않았습니다.")
    }
} 
