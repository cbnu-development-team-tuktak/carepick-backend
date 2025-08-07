package com.callrapport.component.client

import com.callrapport.dto.response.OauthUserInfoResponse

interface OauthProviderClient {
    fun getUserInfo(accessToken: String): OauthUserInfoResponse
}