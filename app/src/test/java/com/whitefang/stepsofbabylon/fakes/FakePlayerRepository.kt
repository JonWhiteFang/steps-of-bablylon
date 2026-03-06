package com.whitefang.stepsofbabylon.fakes

import com.whitefang.stepsofbabylon.domain.model.PlayerProfile
import com.whitefang.stepsofbabylon.domain.model.PlayerWallet
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakePlayerRepository(
    initialProfile: PlayerProfile = PlayerProfile()
) : PlayerRepository {

    val profile = MutableStateFlow(initialProfile)

    override fun observeProfile(): Flow<PlayerProfile> = profile
    override fun observeWallet(): Flow<PlayerWallet> = profile.map { it.toWallet() }
    override fun observeTier(): Flow<Int> = profile.map { it.currentTier }

    override suspend fun addSteps(amount: Long) { profile.update { it.copy(stepBalance = it.stepBalance + amount) } }
    override suspend fun spendSteps(amount: Long) { profile.update { it.copy(stepBalance = it.stepBalance - amount) } }
    override suspend fun addGems(amount: Long) { profile.update { it.copy(gems = it.gems + amount) } }
    override suspend fun spendGems(amount: Long) { profile.update { it.copy(gems = it.gems - amount) } }
    override suspend fun addPowerStones(amount: Long) { profile.update { it.copy(powerStones = it.powerStones + amount) } }
    override suspend fun spendPowerStones(amount: Long) { profile.update { it.copy(powerStones = it.powerStones - amount) } }
    override suspend fun addCardDust(amount: Long) {}
    override suspend fun spendCardDust(amount: Long) {}
    override suspend fun updateTier(tier: Int) { profile.update { it.copy(currentTier = tier) } }
    override suspend fun updateHighestUnlockedTier(tier: Int) { profile.update { it.copy(highestUnlockedTier = tier) } }
    override suspend fun updateBestWave(tier: Int, wave: Int) {
        profile.update { it.copy(bestWavePerTier = it.bestWavePerTier + (tier to wave)) }
    }
    override suspend fun ensureProfileExists() {}
}
