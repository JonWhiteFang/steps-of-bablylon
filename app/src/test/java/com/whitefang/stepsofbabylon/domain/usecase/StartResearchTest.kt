package com.whitefang.stepsofbabylon.domain.usecase

import com.whitefang.stepsofbabylon.domain.model.ActiveResearch
import com.whitefang.stepsofbabylon.domain.model.PlayerProfile
import com.whitefang.stepsofbabylon.domain.model.ResearchType
import com.whitefang.stepsofbabylon.fakes.FakeLabRepository
import com.whitefang.stepsofbabylon.fakes.FakePlayerRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StartResearchTest {

    private lateinit var labRepo: FakeLabRepository
    private lateinit var playerRepo: FakePlayerRepository
    private lateinit var useCase: StartResearch

    @BeforeEach
    fun setup() {
        labRepo = FakeLabRepository()
        playerRepo = FakePlayerRepository(PlayerProfile(stepBalance = 100_000))
        useCase = StartResearch(labRepo, playerRepo)
    }

    @Test
    fun `success deducts steps and starts research`() = runTest {
        val result = useCase(ResearchType.DAMAGE_RESEARCH, playerRepo.profile.value.toWallet(), 1, now = 1000L)
        assertTrue(result is StartResearch.Result.Success)
        assertTrue(playerRepo.profile.value.stepBalance < 100_000)
        assertTrue(labRepo.active.value.any { it.type == ResearchType.DAMAGE_RESEARCH })
    }

    @Test
    fun `insufficient steps returns error`() = runTest {
        playerRepo.profile.value = PlayerProfile(stepBalance = 0)
        val result = useCase(ResearchType.DAMAGE_RESEARCH, playerRepo.profile.value.toWallet(), 1)
        assertTrue(result is StartResearch.Result.InsufficientSteps)
    }

    @Test
    fun `max level returns error`() = runTest {
        labRepo.levels.value = labRepo.levels.value + (ResearchType.DAMAGE_RESEARCH to 20)
        val result = useCase(ResearchType.DAMAGE_RESEARCH, playerRepo.profile.value.toWallet(), 1)
        assertTrue(result is StartResearch.Result.MaxLevelReached)
    }

    @Test
    fun `already researching returns error`() = runTest {
        labRepo.active.value = listOf(ActiveResearch(ResearchType.DAMAGE_RESEARCH, 0, 0, 999999))
        val result = useCase(ResearchType.DAMAGE_RESEARCH, playerRepo.profile.value.toWallet(), 2)
        assertTrue(result is StartResearch.Result.AlreadyResearching)
    }

    @Test
    fun `no slot available returns error`() = runTest {
        labRepo.active.value = listOf(ActiveResearch(ResearchType.HEALTH_RESEARCH, 0, 0, 999999))
        val result = useCase(ResearchType.DAMAGE_RESEARCH, playerRepo.profile.value.toWallet(), 1)
        assertTrue(result is StartResearch.Result.NoSlotAvailable)
    }
}
