package com.callrapport.dto

// 엔티티 관련 import
import com.callrapport.model.university.UniversityRank // 대학 랭킹 정보를 담는 엔티티

// UniversityRankDetailsResponse DTO 정의 (대학 랭킹 상세 정보를 반환하는 응답 객체)
data class UniversityRankDetailsResponse(
    val rank: Int, // 대학 순위 (UniversityRank의 id)
    val krName: String, // 대학명 (한글)
    val enName: String, // 대학명 (영문)
    val region: String // 대학이 속한 지역명
) {
    companion object {
        // UniversityRank 엔티티를 UniversityRankDetailsResponse DTO로 변환
        fun from(entity: UniversityRank): UniversityRankDetailsResponse {
            // 지역명이 없으면 "Unknown" 반환
            val regionName = entity.universityRankRegions.firstOrNull()?.region?.name ?: "Unknown"

            return UniversityRankDetailsResponse(
                rank = entity.id, // 순위 
                krName = entity.krName, // 대학명 (한글)
                enName = entity.enName, // 대학명 (영어)
                region = regionName // 지역명
            )
        }
    }
}
