package com.whitefang.stepsofbabylon.domain.usecase

import com.whitefang.stepsofbabylon.domain.model.CardType
import com.whitefang.stepsofbabylon.domain.model.OwnedCard
import com.whitefang.stepsofbabylon.domain.model.PlayerProfile
import com.whitefang.stepsofbabylon.fakes.FakeCardRepository
import com.whitefang.stepsofbabylon.fakes.FakePlayerRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpgradeCardTest {

    private lateinit var cardRepo: FakeCardRepository
    private lateinit var playerRepo: FakePlayerRepository
    private lateinit var useCase: UpgradeCard

    @BeforeEach
    fun setup() {
        cardRepo = FakeCardRepository()
        playerRepo = FakePlayerRepository(PlayerProfile(cardDust = 500))
        useCase = UpgradeCard(cardRepo, playerRepo)
    }

    @Test
    fun `success upgrades and deducts dust`() = runTest {
        val card = OwnedCard(1, CardType.IRON_SKIN, 1, false) // Common: 1 * 10 = 10 dust
        cardRepo.cards.value = listOf(card)
        val result = useCase(card, 500)
        assertTrue(result is UpgradeCard.Result.Upgraded)
        assertEquals(2, (result as UpgradeCard.Result.Upgraded).newLevel)
        assertEquals(10L, result.dustCost)
        assertEquals(490L, playerRepo.profile.value.cardDust)
    }

    @Test
    fun `max level returns error`() = runTest {
        val card = OwnedCard(1, CardType.IRON_SKIN, 5, false)
        val result = useCase(card, 500)
        assertTrue(result is UpgradeCard.Result.MaxLevel)
    }

    @Test
    fun `insufficient dust returns error`() = runTest {
        val card = OwnedCard(1, CardType.VAMPIRIC_TOUCH, 3, false) // Rare: 3 * 25 = 75 dust
        val result = useCase(card, 50)
        assertTrue(result is UpgradeCard.Result.InsufficientDust)
    }

    @Test
    fun `cost scales with rarity`() = runTest {
        val common = OwnedCard(1, CardType.IRON_SKIN, 2, false) // 2 * 10 = 20
        val rare = OwnedCard(2, CardType.VAMPIRIC_TOUCH, 2, false) // 2 * 25 = 50
        val epic = OwnedCard(3, CardType.GLASS_CANNON, 2, false) // 2 * 50 = 100

        val r1 = useCase(common, 500) as UpgradeCard.Result.Upgraded
        val r2 = useCase(rare, 500) as UpgradeCard.Result.Upgraded
        val r3 = useCase(epic, 500) as UpgradeCard.Result.Upgraded

        assertEquals(20L, r1.dustCost)
        assertEquals(50L, r2.dustCost)
        assertEquals(100L, r3.dustCost)
    }
}
