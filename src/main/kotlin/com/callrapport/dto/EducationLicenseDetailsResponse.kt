package com.callrapport.dto

data class EducationLicenseDetailsResponse(
    val universityRank: Int, // 대학 순위 
    val programDescription: String, // 학력/자격면허 내용
    val type: String, // 유형 (예: 교육, 면허 등)
    val matchedKeyword: String, // 매칭된 키워드
    val status: String, // 상태 (예: 수료, 졸업 등)
    val baseScore: Int, // 기준 점수
    val statusModifier: Double, // 상태에 따른 점수 가중치 
    val score: Double, // 가중치 적용 전 점수
    val weightedScore: Double // 대학 순위를 고려한 최종 점수
)
