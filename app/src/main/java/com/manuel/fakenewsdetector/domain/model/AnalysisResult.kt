package com.manuel.fakenewsdetector.domain.model

data class AnalysisResult(
    val verdict: Verdict,
    val confidenceScore: Double,
    val explanation: String,
    val detectedPatterns: List<String> = emptyList(),
    val sourcesChecked: Int = 0,
    val similarReliableArticles: List<String> = emptyList(),
    val blacklistedDomainMatch: Boolean = false,
    val alternativeNewsTitle: String = "",
    val alternativeNewsUrl: String = "",
    val alternativeNewsDescription: String = ""
)
