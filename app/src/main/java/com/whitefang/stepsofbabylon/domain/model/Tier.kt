package com.whitefang.stepsofbabylon.domain.model

data class Tier(
    val number: Int,
    val unlockWaveRequirement: Int,
    val unlockTierRequirement: Int,
    val cashMultiplier: Double,
    val battleConditions: Map<BattleCondition, Int>,
)
