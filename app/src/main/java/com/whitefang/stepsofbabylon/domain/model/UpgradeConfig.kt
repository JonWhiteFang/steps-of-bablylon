package com.whitefang.stepsofbabylon.domain.model

data class UpgradeConfig(
    val baseCost: Long,
    val scaling: Double,
    val maxLevel: Int?,
    val effectPerLevel: Double,
    val description: String,
)
