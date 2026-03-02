package com.whitefang.stepsofbabylon.domain.model

enum class ResearchType(
    val baseCostSteps: Long,
    val baseTimeHours: Double,
    val maxLevel: Int,
    val effectPerLevel: Double,
    val description: String,
) {
    DAMAGE_RESEARCH(2_000, 4.0, 20, 5.0, "+5% base damage multiplier"),
    HEALTH_RESEARCH(2_000, 4.0, 20, 5.0, "+5% max health multiplier"),
    CASH_RESEARCH(1_500, 3.0, 20, 5.0, "+5% cash earned multiplier"),
    STEP_EFFICIENCY(5_000, 8.0, 10, 2.0, "+2% bonus steps from walking"),
    WAVE_SKIP(10_000, 24.0, 10, 1.0, "Start rounds at wave X instead of wave 1"),
    AUTO_UPGRADE_AI(8_000, 12.0, 5, 1.0, "Auto-spends cash on optimal upgrades during rounds"),
    UW_COOLDOWN(4_000, 6.0, 15, 3.0, "-3% Ultimate Weapon cooldown"),
    CRITICAL_RESEARCH(3_000, 5.0, 15, 3.0, "+3% critical damage multiplier"),
    REGEN_RESEARCH(2_500, 4.5, 15, 4.0, "+4% health regen multiplier"),
    ENEMY_INTEL(3_000, 6.0, 3, 1.0, "Show enemy health bars and incoming wave preview"),
}
