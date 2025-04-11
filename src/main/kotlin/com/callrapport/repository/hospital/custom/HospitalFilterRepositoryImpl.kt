// package com.callrapport.repository.hospital.custom

// import com.callrapport.model.hospital.Hospital
// import jakarta.persistence.EntityManager
// import jakarta.persistence.PersistenceContext
// import org.locationtech.jts.geom.Point
// import org.springframework.data.domain.Page
// import org.springframework.data.domain.PageImpl
// import org.springframework.data.domain.Pageable
// import org.springframework.stereotype.Repository

// @Repository
// class HospitalFilterRepositoryImpl : HospitalFilterRepository {

//     @PersistenceContext
//     private lateinit var em: EntityManager

//     override fun searchHospitalsWithFilters(
//         location: Point?,
//         maxDistanceInMeters: Double?,
//         specialties: List<String>?,
//         selectedDays: List<String>?,
//         startTime: String?,
//         endTime: String?,
//         duration: Int?,
//         sortBy: String,
//         pageable: Pageable
//     ): Page<Hospital> {
//         val cb = em.criteriaBuilder
//         val cq = cb.createQuery(Hospital::class.java)
//         val root = cq.from(Hospital::class.java)
//         cq.select(root).distinct(true)

//         val predicates = mutableListOf<javax.persistence.criteria.Predicate>()

//         // 진료과 필터
//         if (!specialties.isNullOrEmpty()) {
//             val join = root.join<Any, Any>("specialties").join<Any, Any>("specialty")
//             predicates.add(join.get<String>("name").`in`(specialties))
//         }

//         // 거리 필터
//         if (location != null && maxDistanceInMeters != null) {
//             predicates.add(
//                 cb.lessThanOrEqualTo(
//                     cb.function(
//                         "ST_Distance_Sphere",
//                         Double::class.java,
//                         root.get("location"),
//                         cb.literal(location)
//                     ),
//                     maxDistanceInMeters
//                 )
//             )
//         }

//         // 운영 시간 필터 (예: 특정 요일에 운영 중인지, 시간 범위가 포함되는지 등)
//         // → 이 부분은 병원 운영 시간 테이블(HospitalOperatingHours 등)과 join 필요
//         // → 여기에 나중에 조건 추가 가능하도록 TODO로 둠
//         // TODO: selectedDays, startTime, endTime, duration 조건 추가

//         cq.where(*predicates.toTypedArray())

//         // 정렬 조건
//         when (sortBy) {
//             "distance" -> {
//                 if (location != null) {
//                     cq.orderBy(
//                         cb.asc(
//                             cb.function(
//                                 "ST_Distance_Sphere",
//                                 Double::class.java,
//                                 root.get("location"),
//                                 cb.literal(location)
//                             )
//                         )
//                     )
//                 }
//             }

//             "name" -> cq.orderBy(cb.asc(root.get<String>("name")))
//         }

//         // 페이징 처리
//         val query = em.createQuery(cq)
//         query.firstResult = pageable.offset.toInt()
//         query.maxResults = pageable.pageSize

//         // Count 쿼리
//         val countQuery = em.createQuery("SELECT COUNT(DISTINCT h) FROM Hospital h", java.lang.Long::class.java)
//         val total = countQuery.singleResult

//         return PageImpl(query.resultList, pageable, total)
//     }
// }
