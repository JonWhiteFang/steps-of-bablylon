package com.whitefang.stepsofbabylon.domain.usecase

import com.whitefang.stepsofbabylon.data.local.MilestoneEntity
import com.whitefang.stepsofbabylon.domain.model.Milestone
import com.whitefang.stepsofbabylon.fakes.FakeMilestoneDao
import com.whitefang.stepsofbabylon.fakes.FakePlayerRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ClaimMilestoneTest {

    private lateinit var dao: FakeMilestoneDao
    private lateinit var playerRepo: FakePlayerRepository
    private lateinit var useCase: ClaimMilestone

    @BeforeEach
    fun setup() {
        dao = FakeMilestoneDao()
        playerRepo = FakePlayerRepository()
        useCase = ClaimMilestone(dao, playerRepo)
    }

    @Test
    fun `credits Gems correctly`() = runTest {
        val result = useCase(Milestone.MORNING_JOGGER)
        assertTrue(result)
        assertEquals(25, playerRepo.observeWallet().first().gems)
    }

    @Test
    fun `credits Gems and Power Stones for IRON_SOLES`() = runTest {
        useCase(Milestone.IRON_SOLES)
        val wallet = playerRepo.observeWallet().first()
        assertEquals(200, wallet.gems)
        assertEquals(50, wallet.powerStones)
    }

    @Test
    fun `marks milestone as claimed`() = runTest {
        useCase(Milestone.FIRST_STEPS)
        val entity = dao.getByIdOnce(Milestone.FIRST_STEPS.name)
        assertNotNull(entity)
        assertTrue(entity!!.claimed)
        assertNotNull(entity.claimedAt)
    }

    @Test
    fun `claiming twice is no-op`() = runTest {
        useCase(Milestone.MORNING_JOGGER)
        val secondResult = useCase(Milestone.MORNING_JOGGER)
        assertFalse(secondResult)
        assertEquals(25, playerRepo.observeWallet().first().gems) // Not doubled
    }
}
