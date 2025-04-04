package com.callrapport.controller.selfdiagnosis

import com.callrapport.service.selfdiagnosis.SelfDiagnosisService
import com.callrapport.service.selfdiagnosis.DiagnosisResult
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api/self-diagnosis")
class SelfDiagnosisController(
    private val selfDiagnosisService: SelfDiagnosisService
) {

    @PostMapping("/keywords")
    fun handleKeywords(@RequestBody symptoms: List<String>): ResponseEntity<DiagnosisResult> {
        val response = selfDiagnosisService.generateResponse(symptoms)
        return ResponseEntity.ok(response)
    }
}
