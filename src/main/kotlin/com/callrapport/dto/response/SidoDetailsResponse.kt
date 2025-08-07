package com.callrapport.dto

import com.callrapport.model.administrativeRegion.Sido // Sido 엔티티를 가져오기

data class SidoDetailsResponse(
    val name: String,
    val type: String
) {
    companion object {
        fun from(sido: Sido): SidoDetailsResponse {
            return SidoDetailsResponse(
                name = sido.name,
                type = sido.type
            )
        }
    }
}