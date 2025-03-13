package com.callrapport.repository.user

import com.callrapport.model.user.UserFavoriteHospital
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserFavoriteHospitalRepository : JpaRepository<UserFavoriteHospital, Long> {
}
