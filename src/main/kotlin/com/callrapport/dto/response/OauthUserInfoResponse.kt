package com.callrapport.dto.response

import com.callrapport.model.user.OauthProvider

data class OauthUserInfoResponse(
    val provider: OauthProvider, 
    val providerUserId: String, 
    val email: String?,
    val nickname: String?,
    val profileImageUrl: String?
)