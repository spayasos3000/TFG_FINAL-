package com.manuel.fakenewsdetector.domain.model

data class BlacklistedDomain(
    val domain: String,
    val reason: String,
    val addedAt: Long = System.currentTimeMillis(),
    val source: String? = null
)
