package com.whitefang.stepsofbabylon.domain.model

data class SupplyDrop(
    val id: Int,
    val trigger: SupplyDropTrigger,
    val reward: SupplyDropReward,
    val rewardAmount: Int,
    val claimed: Boolean,
    val createdAt: Long,
    val claimedAt: Long? = null,
)
