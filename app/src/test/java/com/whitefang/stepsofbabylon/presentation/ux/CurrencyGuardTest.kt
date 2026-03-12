package com.whitefang.stepsofbabylon.presentation.ux

import com.whitefang.stepsofbabylon.domain.model.PlayerProfile
import com.whitefang.stepsofbabylon.fakes.FakePlayerRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CurrencyGuardTest {

    @Test
    fun `spending more gems than balance clamps to zero`() = runTest {
        val repo = FakePlayerRepository(PlayerProfile(gems = 10))
        repo.spendGems(50)
        assertEquals(0L, repo.profile.value.gems)
    }

    @Test
    fun `spending more power stones than balance clamps to zero`() = runTest {
        val repo = FakePlayerRepository(PlayerProfile(powerStones = 5))
        repo.spendPowerStones(20)
        assertEquals(0L, repo.profile.value.powerStones)
    }

    @Test
    fun `spending more card dust than balance clamps to zero`() = runTest {
        val repo = FakePlayerRepository(PlayerProfile(cardDust = 3))
        repo.spendCardDust(100)
        assertEquals(0L, repo.profile.value.cardDust)
    }

    @Test
    fun `spending more steps than balance clamps to zero`() = runTest {
        val repo = FakePlayerRepository(PlayerProfile(stepBalance = 100))
        repo.spendSteps(500)
        assertEquals(0L, repo.profile.value.stepBalance)
    }
}
