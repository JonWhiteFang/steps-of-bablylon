package com.whitefang.stepsofbabylon.domain.usecase

import com.whitefang.stepsofbabylon.domain.model.SupplyDrop
import com.whitefang.stepsofbabylon.domain.model.SupplyDropReward
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository
import com.whitefang.stepsofbabylon.domain.repository.WalkingEncounterRepository

class ClaimSupplyDrop(
    private val encounterRepository: WalkingEncounterRepository,
    private val playerRepository: PlayerRepository,
) {
    sealed class Result {
        data object Success : Result()
        data object AlreadyClaimed : Result()
    }

    suspend operator fun invoke(drop: SupplyDrop): Result {
        if (drop.claimed) return Result.AlreadyClaimed

        when (drop.reward) {
            SupplyDropReward.STEPS -> playerRepository.addSteps(drop.rewardAmount.toLong())
            SupplyDropReward.GEMS -> playerRepository.addGems(drop.rewardAmount.toLong())
            SupplyDropReward.POWER_STONES -> playerRepository.addPowerStones(drop.rewardAmount.toLong())
            SupplyDropReward.CARD_DUST -> playerRepository.addCardDust(drop.rewardAmount.toLong())
        }
        encounterRepository.claimDrop(drop.id)
        return Result.Success
    }
}
