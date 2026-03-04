package com.whitefang.stepsofbabylon.domain.repository

import com.whitefang.stepsofbabylon.domain.model.PlayerProfile
import com.whitefang.stepsofbabylon.domain.model.PlayerWallet
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun observeProfile(): Flow<PlayerProfile>
    fun observeWallet(): Flow<PlayerWallet>
    fun observeTier(): Flow<Int>
    suspend fun addSteps(amount: Long)
    suspend fun spendSteps(amount: Long)
    suspend fun addGems(amount: Long)
    suspend fun spendGems(amount: Long)
    suspend fun addPowerStones(amount: Long)
    suspend fun spendPowerStones(amount: Long)
    suspend fun addCardDust(amount: Long)
    suspend fun spendCardDust(amount: Long)
    suspend fun updateTier(tier: Int)
    suspend fun updateBestWave(tier: Int, wave: Int)
    suspend fun ensureProfileExists()
}
