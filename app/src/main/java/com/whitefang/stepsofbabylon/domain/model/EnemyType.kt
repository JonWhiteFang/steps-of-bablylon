package com.whitefang.stepsofbabylon.domain.model

enum class EnemyType(
    val speedMultiplier: Double,
    val healthMultiplier: Double,
    val damageMultiplier: Double,
    val description: String,
) {
    BASIC(1.0, 1.0, 1.0, "Normal speed, attacks on contact"),
    FAST(2.0, 0.5, 0.7, "2x speed, lower health, overwhelms in groups"),
    TANK(0.5, 5.0, 2.0, "Slow, massive health, high damage"),
    RANGED(0.8, 0.8, 1.2, "Stops at distance, fires projectiles"),
    BOSS(0.5, 20.0, 3.0, "Every 10 waves, huge health, special abilities"),
    SCATTER(1.2, 1.5, 0.8, "Splits into smaller enemies when killed"),
}
