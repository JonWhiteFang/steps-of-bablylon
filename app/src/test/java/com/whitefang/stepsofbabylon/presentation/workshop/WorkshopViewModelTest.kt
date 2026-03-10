package com.whitefang.stepsofbabylon.presentation.workshop

import com.whitefang.stepsofbabylon.domain.model.PlayerProfile
import com.whitefang.stepsofbabylon.domain.model.UpgradeCategory
import com.whitefang.stepsofbabylon.domain.model.UpgradeType
import com.whitefang.stepsofbabylon.fakes.FakePlayerRepository
import com.whitefang.stepsofbabylon.fakes.FakeWorkshopRepository
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class WorkshopViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var workshopRepo: FakeWorkshopRepository
    private lateinit var playerRepo: FakePlayerRepository
    private val dailyMissionDao = mock<com.whitefang.stepsofbabylon.data.local.DailyMissionDao>()

    @BeforeEach
    fun setup() = runTest(dispatcher) {
        Dispatchers.setMain(dispatcher)
        workshopRepo = FakeWorkshopRepository()
        playerRepo = FakePlayerRepository(PlayerProfile(stepBalance = 10_000))
        workshopRepo.upgrades.value = UpgradeType.entries.associateWith { 0 }
        whenever(dailyMissionDao.getByDateOnce(org.mockito.kotlin.any())).thenReturn(emptyList())
    }

    @AfterEach
    fun tearDown() { Dispatchers.resetMain() }

    private fun createVm() = WorkshopViewModel(workshopRepo, playerRepo, dailyMissionDao)

    @Test
    fun `initial state shows ATTACK upgrades`() = runTest(dispatcher) {
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(UpgradeCategory.ATTACK, state.selectedCategory)
        assertTrue(state.upgrades.all { it.type.category == UpgradeCategory.ATTACK })
        assertTrue(state.upgrades.isNotEmpty())
    }

    @Test
    fun `category switching filters correctly`() = runTest(dispatcher) {
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.selectCategory(UpgradeCategory.DEFENSE)
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(UpgradeCategory.DEFENSE, state.selectedCategory)
        assertTrue(state.upgrades.all { it.type.category == UpgradeCategory.DEFENSE })
    }

    @Test
    fun `purchase deducts steps and increments level`() = runTest(dispatcher) {
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        val upgrade = vm.uiState.value.upgrades.first()
        val costBefore = upgrade.cost
        vm.purchase(upgrade.type)
        advanceUntilIdle()
        val state = vm.uiState.value
        val after = state.upgrades.find { it.type == upgrade.type }!!
        assertEquals(1, after.level)
        assertEquals(10_000 - costBefore, state.stepBalance)
    }

    @Test
    fun `purchase when unaffordable is no-op`() = runTest(dispatcher) {
        playerRepo.profile.value = PlayerProfile(stepBalance = 0)
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        val upgrade = vm.uiState.value.upgrades.first()
        vm.purchase(upgrade.type)
        advanceUntilIdle()
        val after = vm.uiState.value.upgrades.find { it.type == upgrade.type }!!
        assertEquals(0, after.level)
    }

    @Test
    fun `step balance shown`() = runTest(dispatcher) {
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(10_000, vm.uiState.value.stepBalance)
    }

    @Test
    fun `maxed upgrade shows isMaxed`() = runTest(dispatcher) {
        val typeWithMax = UpgradeType.entries.first { it.config.maxLevel != null }
        workshopRepo.upgrades.value = workshopRepo.upgrades.value + (typeWithMax to typeWithMax.config.maxLevel!!)
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        vm.selectCategory(typeWithMax.category)
        advanceUntilIdle()
        val item = vm.uiState.value.upgrades.find { it.type == typeWithMax }!!
        assertTrue(item.isMaxed)
    }
}
