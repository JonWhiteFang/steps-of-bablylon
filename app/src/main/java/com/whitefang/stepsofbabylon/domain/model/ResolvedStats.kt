package com.whitefang.stepsofbabylon.domain.model

data class ResolvedStats(
    val damage: Double = ZigguratBaseStats.BASE_DAMAGE,
    val attackSpeed: Double = ZigguratBaseStats.BASE_ATTACK_SPEED,
    val critChance: Double = 0.0,
    val critMultiplier: Double = 2.0,
    val range: Float = ZigguratBaseStats.BASE_RANGE,
    val maxHealth: Double = ZigguratBaseStats.BASE_HEALTH,
    val healthRegen: Double = ZigguratBaseStats.BASE_REGEN,
    val defensePercent: Double = 0.0,
    val defenseAbsolute: Double = 0.0,
    val knockbackForce: Float = ZigguratBaseStats.BASE_KNOCKBACK,
    val thornPercent: Double = 0.0,
    val lifestealPercent: Double = 0.0,
    val damagePerMeterBonus: Double = 0.0,
    val deathDefyChance: Double = 0.0,
    val multishotTargets: Int = 1,
    val bounceCount: Int = 0,
    val orbCount: Int = 0,
)
