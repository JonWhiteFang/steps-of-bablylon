package com.whitefang.stepsofbabylon.presentation.battle

data class BattleUiState(
    val currentWave: Int = 1,
    val currentHp: Double = 0.0,
    val maxHp: Double = 0.0,
    val cash: Long = 0,
    val speedMultiplier: Float = 1f,
    val isPaused: Boolean = false,
    val isLoading: Boolean = true,
)
