package com.callrapport.dto

data class EducationLicenseDetailsResponse(
    val universityRank: Int, // 대학 순위 
    val programDescription: String, // 학력/자격면허 내용
    val type: String, // 유형 (예: 교육, 면허 등)
    val matchedKeyword: String, 
    val status: String,     
    val baseScore: Int,              
    val statusModifier: Double,      
    val score: Double,
    val weightedScore: Double
)
