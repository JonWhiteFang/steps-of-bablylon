package com.whitefang.stepsofbabylon.fakes

import com.whitefang.stepsofbabylon.domain.model.SupplyDrop
import com.whitefang.stepsofbabylon.domain.model.SupplyDropReward
import com.whitefang.stepsofbabylon.domain.model.SupplyDropTrigger
import com.whitefang.stepsofbabylon.domain.repository.WalkingEncounterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeWalkingEncounterRepository : WalkingEncounterRepository {

    private val drops = MutableStateFlow<List<SupplyDrop>>(emptyList())
    private var nextId = 1

    override fun observeUnclaimed(): Flow<List<SupplyDrop>> =
        drops.map { list -> list.filter { !it.claimed } }

    override fun observeHistory(limit: Int): Flow<List<SupplyDrop>> =
        drops.map { it.take(limit) }

    override fun countUnclaimed(): Flow<Int> =
        drops.map { list -> list.count { !it.claimed } }

    override suspend fun getUnclaimedCount(): Int =
        drops.value.count { !it.claimed }

    override suspend fun createDrop(trigger: SupplyDropTrigger, reward: SupplyDropReward, rewardAmount: Int): Long {
        val id = nextId++
        val drop = SupplyDrop(id = id, trigger = trigger, reward = reward, rewardAmount = rewardAmount, claimed = false, createdAt = System.currentTimeMillis())
        drops.value = drops.value + drop
        return id.toLong()
    }

    override suspend fun claimDrop(id: Int) {
        drops.value = drops.value.map {
            if (it.id == id) it.copy(claimed = true, claimedAt = System.currentTimeMillis()) else it
        }
    }

    override suspend fun enforceInboxCap(maxSize: Int) {
        val unclaimed = drops.value.filter { !it.claimed }
        if (unclaimed.size >= maxSize) {
            val oldest = unclaimed.minByOrNull { it.createdAt } ?: return
            drops.value = drops.value.filter { it.id != oldest.id }
        }
    }
}
