package com.callrapport.repository.hospital

import com.callrapport.model.hospital.AdditionalInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface AdditionalInfoRepository : JpaRepository<AdditionalInfo, String> {
}
