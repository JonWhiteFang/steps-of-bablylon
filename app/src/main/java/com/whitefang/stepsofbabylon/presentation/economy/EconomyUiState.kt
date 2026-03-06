package com.whitefang.stepsofbabylon.presentation.economy

data class EconomyUiState(
    val gems: Long = 0,
    val powerStones: Long = 0,
    val weeklySteps: Long = 0,
    val weeklyClaimedTier: Int = 0,
    val currentStreak: Int = 0,
    val todayPsClaimed: Boolean = false,
    val todayGemsClaimed: Boolean = false,
    val isLoading: Boolean = true,
)
