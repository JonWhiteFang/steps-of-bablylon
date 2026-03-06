package com.whitefang.stepsofbabylon.domain.usecase

import com.whitefang.stepsofbabylon.domain.model.OwnedCard
import com.whitefang.stepsofbabylon.domain.repository.CardRepository
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository

class UpgradeCard(
    private val cardRepository: CardRepository,
    private val playerRepository: PlayerRepository,
) {
    sealed class Result {
        data class Upgraded(val newLevel: Int, val dustCost: Long) : Result()
        data object MaxLevel : Result()
        data object InsufficientDust : Result()
    }

    suspend operator fun invoke(card: OwnedCard, cardDust: Long): Result {
        if (card.level >= card.type.maxLevel) return Result.MaxLevel
        val cost = card.level * card.type.rarity.upgradeDustPerLevel
        if (cardDust < cost) return Result.InsufficientDust
        playerRepository.spendCardDust(cost)
        cardRepository.upgradeCard(card.id, card.level + 1)
        return Result.Upgraded(card.level + 1, cost)
    }
}
