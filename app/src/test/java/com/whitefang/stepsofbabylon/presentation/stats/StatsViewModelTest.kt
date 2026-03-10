package com.whitefang.stepsofbabylon.presentation.stats

import com.whitefang.stepsofbabylon.domain.model.DailyStepSummary
import com.whitefang.stepsofbabylon.domain.model.PlayerProfile
import com.whitefang.stepsofbabylon.domain.model.UpgradeType
import com.whitefang.stepsofbabylon.fakes.FakePlayerRepository
import com.whitefang.stepsofbabylon.fakes.FakeStepRepository
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var playerRepo: FakePlayerRepository
    private lateinit var stepRepo: FakeStepRepository
    private lateinit var workshopRepo: FakeWorkshopRepository

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(dispatcher)
        playerRepo = FakePlayerRepository()
        stepRepo = FakeStepRepository()
        workshopRepo = FakeWorkshopRepository()
    }

    @AfterEach
    fun tearDown() { Dispatchers.resetMain() }

    private fun createVm() = StatsViewModel(stepRepo, playerRepo, workshopRepo)

    @Test
    fun `initial state populates`() = runTest(dispatcher) {
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `maps profile battle stats`() = runTest(dispatcher) {
        playerRepo.profile.value = PlayerProfile(
            totalRoundsPlayed = 10, totalEnemiesKilled = 500, totalCashEarned = 9999,
            totalGemsEarned = 50, totalGemsSpent = 20, gems = 30,
            totalPowerStonesEarned = 15, totalPowerStonesSpent = 5, powerStones = 10,
            bestWavePerTier = mapOf(1 to 25),
        )
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(10, state.totalRoundsPlayed)
        assertEquals(500, state.totalEnemiesKilled)
        assertEquals(30, state.currentGems)
        assertEquals(mapOf(1 to 25), state.bestWavePerTier)
    }

    @Test
    fun `builds 7 bars for week period`() = runTest(dispatcher) {
        val today = LocalDate.now()
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE
        stepRepo.records.value = mapOf(
            today.format(fmt) to DailyStepSummary(today.format(fmt), creditedSteps = 5000),
        )
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(7, vm.uiState.value.bars.size)
    }

    @Test
    fun `period switching changes bar count`() = runTest(dispatcher) {
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.selectPeriod(StatsPeriod.MONTH)
        advanceUntilIdle()
        assertEquals(30, vm.uiState.value.bars.size)
    }

    @Test
    fun `workshop levels summed`() = runTest(dispatcher) {
        workshopRepo.upgrades.value = mapOf(UpgradeType.DAMAGE to 5, UpgradeType.ATTACK_SPEED to 3)
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(8, vm.uiState.value.totalWorkshopLevels)
    }

    @Test
    fun `days active counted from history`() = runTest(dispatcher) {
        val today = LocalDate.now()
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE
        stepRepo.records.value = mapOf(
            today.format(fmt) to DailyStepSummary(today.format(fmt), creditedSteps = 5000),
            today.minusDays(1).format(fmt) to DailyStepSummary(today.minusDays(1).format(fmt), creditedSteps = 0),
            today.minusDays(2).format(fmt) to DailyStepSummary(today.minusDays(2).format(fmt), creditedSteps = 3000),
        )
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(2, vm.uiState.value.daysActive)
    }
}
