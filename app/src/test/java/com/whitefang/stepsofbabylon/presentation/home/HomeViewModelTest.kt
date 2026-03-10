package com.whitefang.stepsofbabylon.presentation.home

import com.whitefang.stepsofbabylon.domain.model.Biome
import com.whitefang.stepsofbabylon.domain.model.DailyStepSummary
import com.whitefang.stepsofbabylon.domain.model.PlayerProfile
import com.whitefang.stepsofbabylon.fakes.*
import com.whitefang.stepsofbabylon.service.MilestoneNotificationManager
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var playerRepo: FakePlayerRepository
    private lateinit var stepRepo: FakeStepRepository
    private lateinit var workshopRepo: FakeWorkshopRepository
    private lateinit var labRepo: FakeLabRepository
    private lateinit var encounterRepo: FakeWalkingEncounterRepository
    private val milestoneDao = FakeMilestoneDao()
    private val dailyMissionDao = FakeDailyMissionDao()
    private val dailyLoginDao = mock<com.whitefang.stepsofbabylon.data.local.DailyLoginDao>()
    private val milestoneNotificationManager = mock<MilestoneNotificationManager>()

    @BeforeEach
    fun setup() = runTest(dispatcher) {
        Dispatchers.setMain(dispatcher)
        playerRepo = FakePlayerRepository(PlayerProfile(
            stepBalance = 5000, gems = 100, powerStones = 20,
            currentTier = 2, highestUnlockedTier = 3,
            bestWavePerTier = mapOf(1 to 15, 2 to 10),
        ))
        stepRepo = FakeStepRepository()
        workshopRepo = FakeWorkshopRepository()
        labRepo = FakeLabRepository()
        encounterRepo = FakeWalkingEncounterRepository()
        whenever(dailyLoginDao.getByDate(org.mockito.kotlin.any())).thenReturn(null)
    }

    @AfterEach
    fun tearDown() { Dispatchers.resetMain() }

    private fun createVm() = HomeViewModel(
        playerRepo, stepRepo, workshopRepo, labRepo, encounterRepo,
        dailyLoginDao, dailyMissionDao, milestoneDao, milestoneNotificationManager,
    )

    @Test
    fun `maps profile to UI state`() = runTest(dispatcher) {
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.currentTier)
        assertEquals(3, state.highestUnlockedTier)
        assertEquals(Biome.HANGING_GARDENS, state.currentBiome)
        assertTrue(state.stepBalance > 0)
    }

    @Test
    fun `todaySteps from step repository`() = runTest(dispatcher) {
        val today = LocalDate.now().toString()
        stepRepo.records.value = mapOf(today to DailyStepSummary(today, creditedSteps = 7500))
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(7500, vm.uiState.value.todaySteps)
    }

    @Test
    fun `bestWave for current tier`() = runTest(dispatcher) {
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(10, vm.uiState.value.bestWave)
    }

    @Test
    fun `unclaimed drop count`() = runTest(dispatcher) {
        encounterRepo.createDrop(
            com.whitefang.stepsofbabylon.domain.model.SupplyDropTrigger.RANDOM,
            com.whitefang.stepsofbabylon.domain.model.SupplyDropReward.GEMS, 5
        )
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.unclaimedDropCount)
    }

    @Test
    fun `selectTier updates profile`() = runTest(dispatcher) {
        val vm = createVm()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.selectTier(3)
        advanceUntilIdle()
        assertEquals(3, playerRepo.profile.value.currentTier)
    }
}
