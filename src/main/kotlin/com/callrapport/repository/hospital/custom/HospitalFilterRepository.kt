// package com.callrapport.repository.hospital.custom

// import com.callrapport.model.hospital.Hospital
// import org.locationtech.jts.geom.Point
// import org.springframework.data.domain.Page
// import org.springframework.data.domain.Pageable

// interface HospitalFilterRepository {
//     fun searchHospitalsWithFilters(
//         location: Point?,
//         maxDistanceInMeters: Double?,
//         specialties: List<String>?,
//         selectedDays: List<String>?,
//         startTime: String?,
//         endTime: String?,
//         duration: Int?,
//         sortBy: String,
//         pageable: Pageable
//     ): Page<Hospital>
// }
