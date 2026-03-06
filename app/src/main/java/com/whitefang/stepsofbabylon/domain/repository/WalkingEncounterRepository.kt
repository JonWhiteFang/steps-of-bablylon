package com.whitefang.stepsofbabylon.domain.repository

import com.whitefang.stepsofbabylon.domain.model.SupplyDrop
import com.whitefang.stepsofbabylon.domain.model.SupplyDropReward
import com.whitefang.stepsofbabylon.domain.model.SupplyDropTrigger
import kotlinx.coroutines.flow.Flow

interface WalkingEncounterRepository {
    fun observeUnclaimed(): Flow<List<SupplyDrop>>
    fun observeHistory(limit: Int): Flow<List<SupplyDrop>>
    fun countUnclaimed(): Flow<Int>
    suspend fun getUnclaimedCount(): Int
    suspend fun createDrop(trigger: SupplyDropTrigger, reward: SupplyDropReward, rewardAmount: Int): Long
    suspend fun claimDrop(id: Int)
    suspend fun enforceInboxCap(maxSize: Int)
}
