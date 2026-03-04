package com.whitefang.stepsofbabylon.domain.model

data class SupplyDrop(
    val id: Int,
    val triggerType: String,
    val rewardType: String,
    val rewardAmount: Int,
    val claimed: Boolean,
    val createdAt: Long,
    val claimedAt: Long? = null,
)
