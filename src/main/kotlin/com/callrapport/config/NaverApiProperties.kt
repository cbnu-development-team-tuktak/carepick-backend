package com.callrapport.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "naver")
data class NaverApiProperties(
    var clientId: String = "zb92fols6g",
    var secret: String = "D22P4B37g3yJpLYDqjrLMfqlqadJby3fLkipdncy" 
)

taehkfdsakf