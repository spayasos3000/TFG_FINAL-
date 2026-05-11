package com.manuel.fakenewsdetector.domain.model

data class NewsAnalysis(
    val id: String,
    val userId: String? = null,
    val url: String? = null,
    val headline: String? = null,
    val content: String? = null,
    val result: AnalysisResult? = null,
    val analyzedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val notes: String? = null
)
