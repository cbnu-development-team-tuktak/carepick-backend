package com.callrapport.repository.user

import com.callrapport.model.user.Oauth
import com.callrapport.model.user.OauthProvider
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OauthRepository : JpaRepository<Oauth, Long> {
    fun findByProviderAndProviderUserId(provider: OauthProvider, providerUserId: String): Oauth?
}