package com.whitefang.stepsofbabylon.domain.usecase

import com.whitefang.stepsofbabylon.domain.model.PlayerProfile
import com.whitefang.stepsofbabylon.domain.model.UpgradeType
import com.whitefang.stepsofbabylon.fakes.FakePlayerRepository
import com.whitefang.stepsofbabylon.fakes.FakeWorkshopRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PurchaseUpgradeTest {

    private val playerRepo = FakePlayerRepository(PlayerProfile(stepBalance = 1000))
    private val workshopRepo = FakeWorkshopRepository()
    private val sut = PurchaseUpgrade(workshopRepo, playerRepo)

    @Test
    fun `successful purchase deducts steps and increments level`() = runTest {
        val wallet = playerRepo.profile.value.toWallet()
        val result = sut(UpgradeType.DAMAGE, 0, wallet)
        assertTrue(result)
        assertEquals(1000 - 50, playerRepo.profile.value.stepBalance) // DAMAGE baseCost=50
        assertEquals(1, workshopRepo.upgrades.value[UpgradeType.DAMAGE])
    }

    @Test
    fun `insufficient steps returns false without mutation`() = runTest {
        val repo = FakePlayerRepository(PlayerProfile(stepBalance = 10))
        val sut = PurchaseUpgrade(workshopRepo, repo)
        val wallet = repo.profile.value.toWallet()
        val result = sut(UpgradeType.DAMAGE, 0, wallet)
        assertFalse(result)
        assertEquals(10, repo.profile.value.stepBalance)
    }

    @Test
    fun `at max level returns false`() = runTest {
        // ORBS maxLevel=6
        val wallet = playerRepo.profile.value.toWallet()
        val result = sut(UpgradeType.ORBS, 6, wallet)
        assertFalse(result)
    }

    @Test
    fun `level 0 purchase costs exactly baseCost`() = runTest {
        val repo = FakePlayerRepository(PlayerProfile(stepBalance = 50))
        val sut = PurchaseUpgrade(workshopRepo, repo)
        val wallet = repo.profile.value.toWallet()
        assertTrue(sut(UpgradeType.DAMAGE, 0, wallet))
        assertEquals(0, repo.profile.value.stepBalance)
    }
}
