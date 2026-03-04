package com.whitefang.stepsofbabylon.data.repository

import com.whitefang.stepsofbabylon.data.local.WalkingEncounterDao
import com.whitefang.stepsofbabylon.data.local.WalkingEncounterEntity
import com.whitefang.stepsofbabylon.domain.model.SupplyDrop
import com.whitefang.stepsofbabylon.domain.repository.WalkingEncounterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WalkingEncounterRepositoryImpl @Inject constructor(
    private val dao: WalkingEncounterDao,
) : WalkingEncounterRepository {

    override fun observeUnclaimed(): Flow<List<SupplyDrop>> =
        dao.getUnclaimed().map { list -> list.map { it.toDomain() } }

    override fun observeHistory(limit: Int): Flow<List<SupplyDrop>> =
        dao.getHistory(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun createDrop(triggerType: String, rewardType: String, rewardAmount: Int): Long =
        dao.insert(WalkingEncounterEntity(
            triggerType = triggerType,
            rewardType = rewardType,
            rewardAmount = rewardAmount,
            createdAt = System.currentTimeMillis(),
        ))

    override suspend fun claimDrop(id: Int) =
        dao.markClaimed(id, System.currentTimeMillis())

    override fun countUnclaimed(): Flow<Int> = dao.countUnclaimed()

    private fun WalkingEncounterEntity.toDomain() = SupplyDrop(
        id = id,
        triggerType = triggerType,
        rewardType = rewardType,
        rewardAmount = rewardAmount,
        claimed = claimed,
        createdAt = createdAt,
        claimedAt = claimedAt,
    )
}
