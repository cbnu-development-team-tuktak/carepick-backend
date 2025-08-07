package com.callrapport.dto

import com.callrapport.model.administrativeRegion.Umd

data class UmdDetailsResponse(
    val code: String, // 행정구역 코드
    val name: String, // 행정구역 이름
    val type: String, // 행정구역 타입 (예: 읍면동)
    val parentCode: String? // 상위 시/군/구 코드가 들어감
) {
    companion object {
        // 부모 코드가 있는 경우
        fun from(entity: Umd, parentCode: String): UmdDetailsResponse {
            return UmdDetailsResponse(
                code = entity.code,
                name = entity.name,
                type = entity.type,
                parentCode = parentCode
            )
        }

        // 부모 코드가 없을 때 (전체 조회용)
        fun from(entity: Umd): UmdDetailsResponse {
            return UmdDetailsResponse(
                code = entity.code,
                name = entity.name,
                type = entity.type,
                parentCode = null
            )
        }
    }
}