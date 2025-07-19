package com.callrapport.model.administrativeRegion

import jakarta.persistence.* // JPA 관련 import
import com.callrapport.model.administrativeRegion.Sido // 시도 엔티티 import
import com.callrapport.model.administrativeRegion.Sgg // 시군구 엔티티 import
import com.fasterxml.jackson.annotation.JsonBackReference // JSON 직렬화 관련 import

@Entity
@Table(name = "sido_sgg")
data class SidoSgg(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(
        name = "sido_id",
        nullable = false,
        referencedColumnName = "code"
    )
    @JsonBackReference
    val sido: Sido,

    @ManyToOne
    @JoinColumn(
        name = "sgg_id",
        nullable = false,
        referencedColumnName = "code"
    )
    val sgg: Sgg
)
