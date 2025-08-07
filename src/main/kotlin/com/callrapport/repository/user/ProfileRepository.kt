package com.callrapport.repository.user

import com.callrapport.model.user.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProfileRepository : JpaRepository<Profile, Long> {
    fun findByRealName(realName: String): List<Profile>
}