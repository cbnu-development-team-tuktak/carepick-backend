package com.callrapport.repository.user

import com.callrapport.model.user.Profile
import com.callrapport.model.user.ProfileAddress
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProfileAddressRepository : JpaRepository<ProfileAddress, Long> {
    fun findByProfile(profile: Profile): ProfileAddress?
}