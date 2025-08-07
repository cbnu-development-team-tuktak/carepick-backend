package com.callrapport.model.user

import com.callrapport.model.administrativeRegion.Sido
import com.callrapport.model.administrativeRegion.Sgg
import com.callrapport.model.administrativeRegion.Umd
import jakarta.persistence.*

@Entity
@Table(name = "address")
data class Address(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sido_code", nullable = false)
    val sido: Sido, // 시도 코드

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sgg_code", nullable = false)
    val sgg: Sgg, // 시군구 코드

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "umd_code", nullable = false)
    val umd: Umd, // 읍면동 코드

    @Column(name = "detail_address", nullable = false)
    val detailAddress: String, // 상세 주소
)