package com.whitefang.stepsofbabylon.presentation.store

import com.whitefang.stepsofbabylon.domain.model.CosmeticCategory
import com.whitefang.stepsofbabylon.domain.model.CosmeticItem
import com.whitefang.stepsofbabylon.domain.model.PlayerProfile
import com.whitefang.stepsofbabylon.fakes.FakeBillingManager
import com.whitefang.stepsofbabylon.fakes.FakeCosmeticRepository
import com.whitefang.stepsofbabylon.fakes.FakePlayerRepository
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
class StoreViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var playerRepo: FakePlayerRepository
    private lateinit var billingManager: FakeBillingManager
    private lateinit var cosmeticRepo: FakeCosmeticRepository

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(dispatcher)
        playerRepo = FakePlayerRepository(PlayerProfile(gems = 200, adRemoved = false))
        billingManager = FakeBillingManager()
        cosmeticRepo = FakeCosmeticRepository()
    }

    @AfterEach
    fun tearDown() { Dispatchers.resetMain() }

    private fun createVm() = StoreViewModel(playerRepo, billingManager, cosmeticRepo)

    @Test
    fun `displays gem balance and ad state`() = runTest(dispatcher) {
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(200, vm.uiState.value.gems)
        assertFalse(vm.uiState.value.adRemoved)
    }

    @Test
    fun `displays cosmetics`() = runTest(dispatcher) {
        cosmeticRepo.items.value = listOf(
            CosmeticItem("skin1", CosmeticCategory.ZIGGURAT_SKIN, "Gold Ziggurat", "Shiny", 100)
        )
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.cosmetics.size)
        assertEquals("Gold Ziggurat", vm.uiState.value.cosmetics.first().name)
    }

    @Test
    fun `purchase cosmetic deducts gems`() = runTest(dispatcher) {
        cosmeticRepo.items.value = listOf(
            CosmeticItem("skin1", CosmeticCategory.ZIGGURAT_SKIN, "Gold Ziggurat", "Shiny", 50)
        )
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.purchaseCosmetic("skin1")
        advanceUntilIdle()
        assertEquals(150, playerRepo.profile.value.gems)
        assertTrue(vm.uiState.value.cosmetics.first().isOwned)
    }
}
