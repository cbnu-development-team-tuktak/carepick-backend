package com.callrapport.model.administrativeRegion

import jakarta.persistence.* // JPA 관련 import
import com.callrapport.model.administrativeRegion.SidoSgg // 시도-시군구 관계 엔티티 import

@Entity
@Table(name = "sido")
data class Sido(
    @Id
    @Column(name = "code", length = 10)
    val code: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "type", nullable = false)
    val type: String,

    @OneToMany(mappedBy = "sido", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val sidoSgg: List<SidoSgg> = mutableListOf()
)