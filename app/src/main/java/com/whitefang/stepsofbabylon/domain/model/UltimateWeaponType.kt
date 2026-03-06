package com.whitefang.stepsofbabylon.domain.model

enum class UltimateWeaponType(
    val unlockCost: Int,
    val baseCooldownSeconds: Int,
    val effectDurationSeconds: Int,
    val description: String,
) {
    DEATH_WAVE(50, 60, 0, "Massive damage pulse radiating outward, damages all enemies on screen"),
    CHAIN_LIGHTNING(75, 45, 0, "Arcing electrical damage chaining between enemies"),
    BLACK_HOLE(100, 90, 5, "Gravity well pulling enemies to a point with sustained damage"),
    CHRONO_FIELD(75, 75, 8, "Slows all enemies to 10% speed for duration"),
    POISON_SWAMP(60, 60, 6, "Toxic area dealing % max health damage/sec to grounded enemies"),
    GOLDEN_ZIGGURAT(80, 90, 10, "5x cash earned + 50% damage boost for duration"),
    ;

    fun upgradeCost(currentLevel: Int): Int = unlockCost * 2 * currentLevel
    fun cooldownAtLevel(level: Int): Float = baseCooldownSeconds * (1f - 0.05f * (level - 1))

    companion object {
        const val MAX_LEVEL = 10
    }
}
