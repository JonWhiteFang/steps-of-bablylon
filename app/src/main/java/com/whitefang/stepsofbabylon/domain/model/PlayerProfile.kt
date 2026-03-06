package com.whitefang.stepsofbabylon.domain.model

data class PlayerProfile(
    val id: Int = 1,
    val totalStepsEarned: Long = 0,
    val stepBalance: Long = 0,
    val gems: Long = 0,
    val powerStones: Long = 0,
    val cardDust: Long = 0,
    val currentTier: Int = 1,
    val highestUnlockedTier: Int = 1,
    val bestWavePerTier: Map<Int, Int> = emptyMap(),
    val createdAt: Long = 0,
    val lastActiveAt: Long = 0,
) {
    fun toWallet(): PlayerWallet = PlayerWallet(
        stepBalance = stepBalance,
        gems = gems,
        powerStones = powerStones,
    )
}
