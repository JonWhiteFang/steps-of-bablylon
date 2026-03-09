package com.whitefang.stepsofbabylon.domain.usecase

import com.whitefang.stepsofbabylon.data.local.MilestoneDao
import com.whitefang.stepsofbabylon.data.local.MilestoneEntity
import com.whitefang.stepsofbabylon.domain.model.Milestone
import com.whitefang.stepsofbabylon.domain.model.MilestoneReward
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository

class ClaimMilestone(
    private val milestoneDao: MilestoneDao,
    private val playerRepository: PlayerRepository,
) {
    suspend operator fun invoke(milestone: Milestone): Boolean {
        val existing = milestoneDao.getByIdOnce(milestone.name)
        if (existing?.claimed == true) return false

        for (reward in milestone.rewards) {
            when (reward) {
                is MilestoneReward.Gems -> playerRepository.addGems(reward.amount)
                is MilestoneReward.PowerStones -> playerRepository.addPowerStones(reward.amount)
                is MilestoneReward.Cosmetic -> { /* no-op until cosmetics system exists */ }
            }
        }
        milestoneDao.upsert(MilestoneEntity(milestone.name, claimed = true, claimedAt = System.currentTimeMillis()))
        return true
    }
}
