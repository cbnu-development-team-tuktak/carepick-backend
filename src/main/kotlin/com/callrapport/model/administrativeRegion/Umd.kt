package com.callrapport.model.administrativeRegion

import jakarta.persistence.* // JPA 엔티티 매핑을 위한 어노테이션 포함

@Entity
@Table(name = "umd")
data class Umd(
    @Id
    @Column(name = "code", length = 10) // 읍면동 코드 (Primary Key)
    val code: String, // ex: "1111000000"

    @Column(name = "name", nullable = false) // 읍면동 이름
    val name: String, // ex: "청운효자동"

    @Column(name = "type", nullable = false) // 읍면동 구분
    val type: String // 읍, 면, 동
)
