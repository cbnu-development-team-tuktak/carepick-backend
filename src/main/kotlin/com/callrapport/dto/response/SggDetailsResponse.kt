package com.callrapport.dto 

import com.callrapport.model.administrativeRegion.Sgg // Sgg 엔티티를 가져오기

data class SggDetailsResponse(
    val code: String, // 행정구역 코드
    val name: String, // 행정구역 이름
    val type: String, // 행정구역 타입 (예: 시군구)
    val parentCode: String? // 상위 행정구역 코드 (시도 코드 등, null일 수 있음)
) {
    companion object {
        // 부모 코드가 있는 경우
        fun from(entity: Sgg, parentCode: String): SggDetailsResponse {
            return SggDetailsResponse(
                code = entity.code,
                name = entity.name,
                type = entity.type,
                parentCode = parentCode
            )
        }

        // 부모 코드가 없을 때 (전체 조회용)
        fun from(entity: Sgg): SggDetailsResponse {
            return SggDetailsResponse(
                code = entity.code,
                name = entity.name,
                type = entity.type,
                parentCode = null
            )
        }
    }
}
