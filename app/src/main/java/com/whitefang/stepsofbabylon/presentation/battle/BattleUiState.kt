package com.whitefang.stepsofbabylon.presentation.battle

import com.whitefang.stepsofbabylon.domain.model.UpgradeType
import com.whitefang.stepsofbabylon.presentation.battle.ui.BiomeTransitionInfo

data class RoundEndState(
    val waveReached: Int,
    val enemiesKilled: Int,
    val totalCashEarned: Long,
    val timeSurvivedSeconds: Float,
    val isNewBestWave: Boolean,
    val previousBest: Int,
    val tierUnlocked: Int? = null,
)

data class BattleUiState(
    val currentWave: Int = 1,
    val currentHp: Double = 0.0,
    val maxHp: Double = 0.0,
    val cash: Long = 0,
    val enemyCount: Int = 0,
    val wavePhase: String = "",
    val speedMultiplier: Float = 1f,
    val isPaused: Boolean = false,
    val isLoading: Boolean = true,
    val showUpgradeMenu: Boolean = false,
    val inRoundLevels: Map<UpgradeType, Int> = emptyMap(),
    val lastPurchaseFree: Boolean = false,
    val roundEndState: RoundEndState? = null,
    val biomeTransition: BiomeTransitionInfo? = null,
)
