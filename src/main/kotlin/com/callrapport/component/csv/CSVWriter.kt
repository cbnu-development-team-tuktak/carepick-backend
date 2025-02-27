package com.callrapport.component.csv

import org.springframework.stereotype.Component
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.FileOutputStream

@Component
class CSVWriter {
    fun writeToCSV(data: List<Map<String, String?>>, filePath: String): Boolean {
        return try {
            FileOutputStream(filePath).use { fos ->
                OutputStreamWriter(fos, Charsets.UTF_8).use { osw ->
                    BufferedWriter(osw).use { writer ->
                        // UTF-8 BOM 추가 (엑셀 한글 깨짐 방지)
                        writer.write("\uFEFF")

                        // CSV 컬럼명 작성
                        if (data.isNotEmpty()) {
                            val headers = data.first().keys.toList()
                            writer.write(headers.joinToString(",") { "\"$it\"" })
                            writer.newLine()

                            // 데이터 작성
                            for (entry in data) {
                                val row = headers.map { key ->
                                    val value = entry[key]?.replace("\"", "\"\"") ?: "" // 따옴표 이스케이프
                                    "\"$value\"" // 모든 값을 따옴표로 감싸서 안전한 CSV 형식 유지
                                }
                                writer.write(row.joinToString(","))
                                writer.newLine()
                            }
                        }
                    }
                }
            }
            println("✅ CSV 파일이 성공적으로 생성됨: $filePath")
            true
        } catch (e: Exception) {
            println("⚠️ CSV 작성 실패: ${e.message}")
            false
        }
    }
}
