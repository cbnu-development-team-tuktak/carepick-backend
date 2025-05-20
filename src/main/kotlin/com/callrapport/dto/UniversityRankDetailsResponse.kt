package com.callrapport.dto

import com.callrapport.model.university.UniversityRank

data class UniversityRankDetailsResponse(
    val rank: Int,
    val krName: String,
    val enName: String,
    val region: String
) {
    companion object {
        fun from(entity: UniversityRank): UniversityRankDetailsResponse {
            return UniversityRankDetailsResponse(
                rank = entity.id,
                krName = entity.krName,
                enName = entity.enName,
                region = entity.region
            )
        }
    }
}
