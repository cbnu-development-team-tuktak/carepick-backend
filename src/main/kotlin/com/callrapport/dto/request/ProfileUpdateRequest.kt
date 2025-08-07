package com.callrapport.dto.request

import com.callrapport.model.user.Gender // 성별 열거형
import java.time.LocalDate // 날짜 타입

data class ProfileUpdateRequest(
    val userId: String, // 사용자 ID
    val realName: String? = null, // 실제 이름
    val birthDate: LocalDate? = null, // 생년월일
    val gender: Gender? = null // 성별 (선택적, null 가능) 
)