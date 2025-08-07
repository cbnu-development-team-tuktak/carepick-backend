package com.callrapport.dto.request

data class AddressUpdateRequest(
    val userId: String, // 사용자 ID
    val sidoCode: String, // 시도 코드
    val sggCode: String, // 시군구 코드
    val umdCode: String, // 읍면동 코드
    val detailAddress: String? = null // 상세 주소
)