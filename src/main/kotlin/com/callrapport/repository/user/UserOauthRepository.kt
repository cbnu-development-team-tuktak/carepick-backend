package com.callrapport.repository.user

import com.callrapport.model.user.UserOauth
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserOauthRepository : JpaRepository<UserOauth, Long>