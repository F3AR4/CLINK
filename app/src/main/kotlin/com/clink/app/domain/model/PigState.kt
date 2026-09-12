package com.clink.app.domain.model

/**
 * Represents the progression state of a digital piggy bank in CLINK.
 * The state is derived deterministically from the pig's accumulated savings.
 */
enum class PigState(
    val displayName: String,
    val badgeLabel: String,
    val description: String
) {
    /**
     * Freshly created, empty pig waiting for its very first coin.
     */
    NEW(
        displayName = "New",
        badgeLabel = "🐣 New",
        description = "Empty and waiting for your very first clink!"
    ),

    /**
     * User has begun micro-saving and is building initial savings momentum.
     */
    GROWING(
        displayName = "Growing",
        badgeLabel = "🌱 Growing",
        description = "Great start! Building savings momentum one coin at a time."
    ),

    /**
     * Meaningful savings accumulated; consistent habit formed.
     */
    HEALTHY(
        displayName = "Healthy",
        badgeLabel = "✨ Healthy",
        description = "Looking strong! You've formed a solid savings habit."
    ),

    /**
     * Pig is well-fed, proud, and celebratory!
     */
    FULL(
        displayName = "Full",
        badgeLabel = "🏆 Full",
        description = "Well-fed and thriving! Outstanding savings milestone."
    )
}

/**
 * Rich domain progression details for a pig.
 * Contains current state, next threshold milestone, and normalized progress (0.0f - 1.0f).
 */
data class PigProgression(
    val state: PigState,
    val currentBalance: Money,
    val nextThreshold: Money?,
    val progressToNextTier: Float
)
