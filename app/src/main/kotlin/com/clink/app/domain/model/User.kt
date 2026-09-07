package com.clink.app.domain.model

/**
 * Domain model representing a local user profile.
 */
data class User(
    val id: String,
    val displayName: String,
    val preferredCurrency: String = "INR",
    val createdAt: Long = System.currentTimeMillis()
)
