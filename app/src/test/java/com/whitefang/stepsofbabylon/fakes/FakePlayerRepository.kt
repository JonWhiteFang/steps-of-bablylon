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
    override suspend fun spendSteps(amount: Long) { profile.update { it.copy(stepBalance = maxOf(0, it.stepBalance - amount)) } }
    override suspend fun addGems(amount: Long) { profile.update { it.copy(gems = it.gems + amount, totalGemsEarned = it.totalGemsEarned + amount) } }
    override suspend fun spendGems(amount: Long) { profile.update { it.copy(gems = maxOf(0, it.gems - amount), totalGemsSpent = it.totalGemsSpent + amount) } }
    override suspend fun addPowerStones(amount: Long) { profile.update { it.copy(powerStones = it.powerStones + amount, totalPowerStonesEarned = it.totalPowerStonesEarned + amount) } }
    override suspend fun spendPowerStones(amount: Long) { profile.update { it.copy(powerStones = maxOf(0, it.powerStones - amount), totalPowerStonesSpent = it.totalPowerStonesSpent + amount) } }
    override suspend fun addCardDust(amount: Long) { profile.update { it.copy(cardDust = it.cardDust + amount) } }
    override suspend fun spendCardDust(amount: Long) { profile.update { it.copy(cardDust = maxOf(0, it.cardDust - amount)) } }
    override suspend fun updateTier(tier: Int) { profile.update { it.copy(currentTier = tier) } }
    override suspend fun updateHighestUnlockedTier(tier: Int) { profile.update { it.copy(highestUnlockedTier = tier) } }
    override suspend fun updateLabSlotCount(count: Int) { profile.update { it.copy(labSlotCount = count) } }
    override suspend fun updateStreak(streak: Int, date: String) { profile.update { it.copy(currentStreak = streak, lastLoginDate = date) } }
    override suspend fun incrementBattleStats(rounds: Long, kills: Long, cash: Long) {
        profile.update { it.copy(totalRoundsPlayed = it.totalRoundsPlayed + rounds, totalEnemiesKilled = it.totalEnemiesKilled + kills, totalCashEarned = it.totalCashEarned + cash) }
    }
    override suspend fun updateBestWave(tier: Int, wave: Int) {
        profile.update { it.copy(bestWavePerTier = it.bestWavePerTier + (tier to wave)) }
    }
    override suspend fun updateAdRemoved(removed: Boolean) { profile.update { it.copy(adRemoved = removed) } }
    override suspend fun updateSeasonPass(active: Boolean, expiry: Long) { profile.update { it.copy(seasonPassActive = active, seasonPassExpiry = expiry) } }
    override suspend fun updateFreeLabRushUsed(date: String) { profile.update { it.copy(freeLabRushUsedToday = date) } }
    override suspend fun updateFreeCardPackAdUsed(date: String) { profile.update { it.copy(freeCardPackAdUsedToday = date) } }
    override suspend fun updateLastActiveAt(timestamp: Long) { profile.update { it.copy(lastActiveAt = timestamp) } }
    override suspend fun getStepBalance(): Long = profile.value.stepBalance
    override suspend fun ensureProfileExists() {}
}
