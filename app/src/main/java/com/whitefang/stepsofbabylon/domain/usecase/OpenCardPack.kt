package com.whitefang.stepsofbabylon.domain.usecase

import com.whitefang.stepsofbabylon.domain.model.CardRarity
import com.whitefang.stepsofbabylon.domain.model.CardType
import com.whitefang.stepsofbabylon.domain.model.OwnedCard
import com.whitefang.stepsofbabylon.domain.repository.CardRepository
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository
import kotlin.random.Random

enum class PackTier(val gemCost: Long, val commonWeight: Double, val rareWeight: Double, val epicWeight: Double) {
    COMMON(50, 0.80, 0.18, 0.02),
    RARE(150, 0.50, 0.40, 0.10),
    EPIC(500, 0.20, 0.40, 0.40),
}

data class CardResult(val type: CardType, val isNew: Boolean, val dustAwarded: Long)

class OpenCardPack(
    private val cardRepository: CardRepository,
    private val playerRepository: PlayerRepository,
    private val random: Random = Random,
) {
    sealed class Result {
        data class Opened(val cards: List<CardResult>) : Result()
        data object InsufficientGems : Result()
    }

    suspend operator fun invoke(packTier: PackTier, gems: Long, ownedCards: List<OwnedCard>, isFree: Boolean = false): Result {
        if (!isFree && gems < packTier.gemCost) return Result.InsufficientGems
        if (!isFree) playerRepository.spendGems(packTier.gemCost)

        val ownedTypes = ownedCards.map { it.type }.toMutableSet()
        val results = mutableListOf<CardResult>()

        repeat(3) {
            val rarity = rollRarity(packTier)
            val candidates = CardType.entries.filter { it.rarity == rarity }
            val type = candidates[random.nextInt(candidates.size)]

            if (type in ownedTypes) {
                val dust = type.rarity.dustValue
                playerRepository.addCardDust(dust)
                results += CardResult(type, isNew = false, dustAwarded = dust)
            } else {
                cardRepository.addCard(type)
                ownedTypes += type
                results += CardResult(type, isNew = true, dustAwarded = 0)
            }
        }
        return Result.Opened(results)
    }

    private fun rollRarity(packTier: PackTier): CardRarity {
        val roll = random.nextDouble()
        return when {
            roll < packTier.commonWeight -> CardRarity.COMMON
            roll < packTier.commonWeight + packTier.rareWeight -> CardRarity.RARE
            else -> CardRarity.EPIC
        }
    }
}
