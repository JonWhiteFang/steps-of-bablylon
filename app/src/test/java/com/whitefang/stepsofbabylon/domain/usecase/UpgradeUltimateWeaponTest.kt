package com.whitefang.stepsofbabylon.domain.usecase

import com.whitefang.stepsofbabylon.domain.model.PlayerProfile
import com.whitefang.stepsofbabylon.domain.model.UltimateWeaponType
import com.whitefang.stepsofbabylon.fakes.FakePlayerRepository
import com.whitefang.stepsofbabylon.fakes.FakeUltimateWeaponRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UpgradeUltimateWeaponTest {

    private val playerRepo = FakePlayerRepository(PlayerProfile(powerStones = 10000))
    private val uwRepo = FakeUltimateWeaponRepository()
    private val sut = UpgradeUltimateWeapon(uwRepo, playerRepo)

    @Test
    fun `upgrade succeeds with sufficient stones`() = runTest {
        // DEATH_WAVE upgradeCost(1) = 50 * 2 * 1 = 100
        assertTrue(sut(UltimateWeaponType.DEATH_WAVE, 1, 10000))
    }

    @Test
    fun `upgrade fails at max level`() = runTest {
        assertFalse(sut(UltimateWeaponType.DEATH_WAVE, 10, 99999))
    }

    @Test
    fun `upgrade fails with insufficient stones`() = runTest {
        // DEATH_WAVE upgradeCost(5) = 50 * 2 * 5 = 500
        assertFalse(sut(UltimateWeaponType.DEATH_WAVE, 5, 499))
    }

    @Test
    fun `upgrade cost scales with level`() = runTest {
        // BLACK_HOLE upgradeCost(3) = 100 * 2 * 3 = 600
        assertTrue(sut(UltimateWeaponType.BLACK_HOLE, 3, 600))
        assertFalse(sut(UltimateWeaponType.BLACK_HOLE, 3, 599))
    }
}
