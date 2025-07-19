package com.callrapport.model.administrativeRegion

import jakarta.persistence.* // JPA 관련 import
import com.fasterxml.jackson.annotation.JsonBackReference // JSON 직렬화 관련 import
import com.callrapport.model.administrativeRegion.Sgg // 시군구 엔티티 import
import com.callrapport.model.administrativeRegion.Umd // 읍면동 엔티티 import

@Entity
@Table(name = "sgg_umd")
data class SggUmd(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가
    val id: Long? = null, // 시군구-읍면동 관계 ID

    @ManyToOne
    @JoinColumn(
        name = "sgg_id", // 매핑할 외래 키(FK) 이름
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        referencedColumnName = "code" // Sgg 엔티티의 "code" 컬럼을 참조
    )
    @JsonBackReference
    val sgg: Sgg, // 상위 시군구

    @ManyToOne
    @JoinColumn(
        name = "umd_id", // 매핑할 외래 키(FK) 이름
        nullable = false, // 필수 입력 값 (NULL 허용 안 함)
        referencedColumnName = "code" // Umd 엔티티의 "code" 컬럼을 참조
    )
    val umd: Umd // 하위 읍면동
)
