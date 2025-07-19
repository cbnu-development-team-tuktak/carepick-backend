package com.callrapport.model.administrativeRegion

import jakarta.persistence.* 

@Entity
@Table(name = "sgg")
data class Sgg(
    @Id
    @Column(name = "code", length = 10) // 시군구 코드 (Primary Key)
    val code: String, // 시군구 코드 (Primary Key)

    @Column(name = "name", nullable = false) // 시군구 이름
    val name: String, // 시군구 이름

    @Column(name = "type", nullable = false) // 시군구 타입
    val type: String, // 시군구 타입
 
    @OneToMany(mappedBy = "sgg", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY) // 시군구에 속한 읍면동 목록
    val sggUmd: List<SggUmd> = mutableListOf() // 시군구에 속한 읍면동 목록
)
