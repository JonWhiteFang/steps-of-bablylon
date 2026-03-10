package com.whitefang.stepsofbabylon.presentation.cards

import com.whitefang.stepsofbabylon.domain.model.CardType
import com.whitefang.stepsofbabylon.domain.model.OwnedCard
import com.whitefang.stepsofbabylon.domain.model.PlayerProfile
import com.whitefang.stepsofbabylon.fakes.FakeCardRepository
import com.whitefang.stepsofbabylon.fakes.FakePlayerRepository
import com.whitefang.stepsofbabylon.fakes.FakeRewardAdManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CardsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var cardRepo: FakeCardRepository
    private lateinit var playerRepo: FakePlayerRepository
    private lateinit var adManager: FakeRewardAdManager

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(dispatcher)
        cardRepo = FakeCardRepository()
        playerRepo = FakePlayerRepository(PlayerProfile(gems = 500, cardDust = 100))
        adManager = FakeRewardAdManager()
    }

    @AfterEach
    fun tearDown() { Dispatchers.resetMain() }

    private fun createVm() = CardsViewModel(cardRepo, playerRepo, adManager)

    @Test
    fun `displays owned cards`() = runTest(dispatcher) {
        cardRepo.cards.value = listOf(
            OwnedCard(1, CardType.SHARP_SHOOTER, 1, false),
            OwnedCard(2, CardType.IRON_SKIN, 2, true),
        )
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(2, state.ownedCards.size)
        assertEquals(1, state.equippedCount)
    }

    @Test
    fun `gem balance and dust shown`() = runTest(dispatcher) {
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(500, vm.uiState.value.gems)
        assertEquals(100, vm.uiState.value.cardDust)
    }

    @Test
    fun `equip toggles card state`() = runTest(dispatcher) {
        cardRepo.cards.value = listOf(OwnedCard(1, CardType.SHARP_SHOOTER, 1, false))
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.equipCard(1)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.ownedCards.first().isEquipped)
    }

    @Test
    fun `unequip toggles card state`() = runTest(dispatcher) {
        cardRepo.cards.value = listOf(OwnedCard(1, CardType.SHARP_SHOOTER, 1, true))
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.unequipCard(1)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.ownedCards.first().isEquipped)
    }

    @Test
    fun `upgrade deducts dust and increments level`() = runTest(dispatcher) {
        cardRepo.cards.value = listOf(OwnedCard(1, CardType.SHARP_SHOOTER, 1, false))
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.upgradeCard(1)
        advanceUntilIdle()
        val card = vm.uiState.value.ownedCards.first()
        assertEquals(2, card.level)
        assertTrue(playerRepo.profile.value.cardDust < 100)
    }
}
