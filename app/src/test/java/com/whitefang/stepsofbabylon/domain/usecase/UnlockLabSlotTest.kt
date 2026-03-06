package com.whitefang.stepsofbabylon.domain.usecase

import com.whitefang.stepsofbabylon.domain.model.PlayerProfile
import com.whitefang.stepsofbabylon.fakes.FakePlayerRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UnlockLabSlotTest {

    private lateinit var playerRepo: FakePlayerRepository
    private lateinit var useCase: UnlockLabSlot

    @BeforeEach
    fun setup() {
        playerRepo = FakePlayerRepository(PlayerProfile(gems = 500))
        useCase = UnlockLabSlot(playerRepo)
    }

    @Test
    fun `success deducts gems and increments slot count`() = runTest {
        val result = useCase(currentSlotCount = 1, gems = 500)
        assertTrue(result is UnlockLabSlot.Result.Unlocked)
        assertEquals(2, (result as UnlockLabSlot.Result.Unlocked).newSlotCount)
        assertEquals(300L, playerRepo.profile.value.gems)
    }

    @Test
    fun `max slots reached returns error`() = runTest {
        val result = useCase(currentSlotCount = 4, gems = 500)
        assertTrue(result is UnlockLabSlot.Result.MaxSlotsReached)
    }

    @Test
    fun `insufficient gems returns error`() = runTest {
        val result = useCase(currentSlotCount = 1, gems = 100)
        assertTrue(result is UnlockLabSlot.Result.InsufficientGems)
    }
}
