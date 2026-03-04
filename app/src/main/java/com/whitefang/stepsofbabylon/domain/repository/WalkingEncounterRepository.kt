package com.whitefang.stepsofbabylon.domain.repository

import com.whitefang.stepsofbabylon.domain.model.SupplyDrop
import kotlinx.coroutines.flow.Flow

interface WalkingEncounterRepository {
    fun observeUnclaimed(): Flow<List<SupplyDrop>>
    fun observeHistory(limit: Int): Flow<List<SupplyDrop>>
    suspend fun createDrop(triggerType: String, rewardType: String, rewardAmount: Int): Long
    suspend fun claimDrop(id: Int)
    fun countUnclaimed(): Flow<Int>
}
