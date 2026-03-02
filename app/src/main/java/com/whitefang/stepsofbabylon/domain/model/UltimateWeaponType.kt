package com.whitefang.stepsofbabylon.domain.model

enum class UltimateWeaponType(
    val unlockCost: Int,
    val description: String,
) {
    DEATH_WAVE(50, "Massive damage pulse radiating outward, damages all enemies on screen"),
    CHAIN_LIGHTNING(75, "Arcing electrical damage chaining between enemies"),
    BLACK_HOLE(100, "Gravity well pulling enemies to a point with sustained damage"),
    CHRONO_FIELD(75, "Slows all enemies to 10% speed for duration"),
    POISON_SWAMP(60, "Toxic area dealing % max health damage/sec to grounded enemies"),
    GOLDEN_ZIGGURAT(80, "5x cash earned + 50% damage boost for duration"),
}
