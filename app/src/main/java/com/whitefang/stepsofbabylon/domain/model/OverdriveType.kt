package com.whitefang.stepsofbabylon.domain.model

enum class OverdriveType(
    val stepCost: Long,
    val durationSeconds: Int,
    val description: String,
) {
    ASSAULT(500, 60, "2x Attack Speed + 1.5x Damage"),
    FORTRESS(500, 60, "2x Health Regen + 50% Damage Reduction"),
    FORTUNE(300, 60, "3x Cash earned from all sources"),
    SURGE(750, 60, "All UW cooldowns reset instantly"),
}
