package com.whitefang.stepsofbabylon.domain.usecase

import com.whitefang.stepsofbabylon.data.local.PlayerProfileDao
import com.whitefang.stepsofbabylon.domain.model.PlayerProfile
import com.whitefang.stepsofbabylon.fakes.FakeDailyStepDao
import com.whitefang.stepsofbabylon.fakes.FakePlayerRepository
import com.whitefang.stepsofbabylon.fakes.FakeTimeProvider
import java.time.LocalDate
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class AwardBattleStepsTest {

    private val today = "2026-05-03"
    private val tomorrow = "2026-05-04"

    /**
     * Builds a fresh [AwardBattleSteps] with a linked player/DAO pair. The `playerProfileDao`
     * arg is a [mock] decoy \u2014 [FakeDailyStepDao.creditBattleStepsAtomic] ignores it and routes
     * wallet updates through `linkedPlayer` so the existing `player.observeProfile()`
     * assertions keep working. Room's generated impl exercises the real DAO path at runtime.
     */
    private fun sut(initialBalance: Long = 0L): Triple<AwardBattleSteps, FakePlayerRepository, FakeDailyStepDao> {
        val playerRepo = FakePlayerRepository(PlayerProfile(stepBalance = initialBalance))
        val dao = FakeDailyStepDao(linkedPlayer = playerRepo)
        val useCase = AwardBattleSteps(dao, mock<PlayerProfileDao>())
        return Triple(useCase, playerRepo, dao)
    }

    @Test
    fun `first call credits the full amount`() = runTest {
        val (useCase, player, dao) = sut()

        val credited = useCase(5L, today)

        assertEquals(5L, credited)
        assertEquals(5L, player.observeProfile().first().stepBalance)
        assertEquals(5L, dao.getBattleStepsEarned(today))
    }

    @Test
    fun `credits 0 and does not touch wallet once cap is hit`() = runTest {
        val (useCase, player, dao) = sut()
        dao.incrementBattleSteps(today, AwardBattleSteps.DAILY_BATTLE_STEP_CAP)

        val credited = useCase(10L, today)

        assertEquals(0L, credited)
        assertEquals(0L, player.observeProfile().first().stepBalance)
        assertEquals(AwardBattleSteps.DAILY_BATTLE_STEP_CAP, dao.getBattleStepsEarned(today))
    }

    @Test
    fun `partial credit when remaining is smaller than amount`() = runTest {
        val (useCase, player, dao) = sut()
        // Start at cap - 3 \u2192 only 3 should be credited out of a 10 request.
        dao.incrementBattleSteps(today, AwardBattleSteps.DAILY_BATTLE_STEP_CAP - 3L)

        val credited = useCase(10L, today)

        assertEquals(3L, credited)
        assertEquals(3L, player.observeProfile().first().stepBalance)
        assertEquals(AwardBattleSteps.DAILY_BATTLE_STEP_CAP, dao.getBattleStepsEarned(today))
    }

    @Test
    fun `date rollover resets the per-day counter`() = runTest {
        val (useCase, player, dao) = sut()
        // Exhaust today's cap.
        dao.incrementBattleSteps(today, AwardBattleSteps.DAILY_BATTLE_STEP_CAP)

        val creditedToday = useCase(5L, today)
        val creditedTomorrow = useCase(5L, tomorrow)

        assertEquals(0L, creditedToday)
        assertEquals(5L, creditedTomorrow)
        assertEquals(5L, player.observeProfile().first().stepBalance)
        assertEquals(AwardBattleSteps.DAILY_BATTLE_STEP_CAP, dao.getBattleStepsEarned(today))
        assertEquals(5L, dao.getBattleStepsEarned(tomorrow))
    }

    @Test
    fun `zero or negative amounts are no-ops`() = runTest {
        val (useCase, player, dao) = sut()

        assertEquals(0L, useCase(0L, today))
        assertEquals(0L, useCase(-5L, today))
        assertEquals(0L, player.observeProfile().first().stepBalance)
        assertEquals(0L, dao.getBattleStepsEarned(today))
    }

    @Test
    fun `dao is incremented by credited amount not requested amount`() = runTest {
        val (useCase, _, dao) = sut()
        // Only 2 headroom in the cap; caller asks for 50.
        dao.incrementBattleSteps(today, AwardBattleSteps.DAILY_BATTLE_STEP_CAP - 2L)

        val credited = useCase(50L, today)

        assertEquals(2L, credited)
        assertEquals(AwardBattleSteps.DAILY_BATTLE_STEP_CAP, dao.getBattleStepsEarned(today))
    }

    @Test
    fun `FakeTimeProvider drives the cap reset when caller omits today`() = runTest {
        // Exercises the default `today` expression in invoke() \u2014 B.1 PR 2
        // rewired that expression from LocalDate.now().toString() to
        // timeProvider.today().toString(). A FakeTimeProvider fed to the
        // constructor now deterministically drives the date bucket without the
        // caller passing it explicitly.
        val playerRepo = FakePlayerRepository(PlayerProfile(stepBalance = 0L))
        val dao = FakeDailyStepDao(linkedPlayer = playerRepo)
        val clock = FakeTimeProvider(fixedDate = LocalDate.parse(today))
        val useCase = AwardBattleSteps(dao, mock<PlayerProfileDao>(), clock)

        // Exhaust today's cap using the default `today` parameter.
        dao.incrementBattleSteps(today, AwardBattleSteps.DAILY_BATTLE_STEP_CAP)
        val creditedBeforeRollover = useCase(10L)
        assertEquals(0L, creditedBeforeRollover, "cap exhausted on day 1")

        // Advance the fake clock across midnight. No caller code changes \u2014
        // the same invoke() call now writes to a different date bucket.
        clock.fixedDate = LocalDate.parse(tomorrow)
        val creditedAfterRollover = useCase(10L)

        assertEquals(10L, creditedAfterRollover, "fresh cap on day 2")
        assertEquals(10L, dao.getBattleStepsEarned(tomorrow))
        assertEquals(AwardBattleSteps.DAILY_BATTLE_STEP_CAP, dao.getBattleStepsEarned(today),
            "day 1 record untouched")
    }

    // -------- B.2 PR 2 atomicity tests --------

    @Test
    fun `successful credit goes through atomic DAO method and bypasses the legacy split path`() = runTest {
        val (useCase, player, dao) = sut()

        assertEquals(7L, useCase(7L, today))

        // Proves AwardBattleSteps now delegates to creditBattleStepsAtomic and no longer makes
        // the two separate `playerRepository.addSteps` + `dao.incrementBattleSteps` calls.
        assertEquals(1, dao.creditBattleStepsAtomicCallCount)
        assertEquals(0, player.spendStepsCallCount, "no direct wallet write outside the atomic path")
    }

    @Test
    fun `two concurrent kills on exactly one headroom - only one credits`() = runTest {
        // Exactly one battle-step of headroom left. Two concurrent kills race. Before B.2 PR 2,
        // both could read alreadyEarned = cap - 1, both compute credited = 1, both addSteps(1) \u2014
        // overflowing the cap by 1 and double-crediting the wallet. The Mutex-guarded atomic
        // method (mirroring the SQL transaction guard) must serialise them so only one succeeds.
        val playerRepo = FakePlayerRepository(PlayerProfile(stepBalance = 0L))
        val dao = FakeDailyStepDao(linkedPlayer = playerRepo)
        val useCase = AwardBattleSteps(dao, mock<PlayerProfileDao>())
        dao.incrementBattleSteps(today, AwardBattleSteps.DAILY_BATTLE_STEP_CAP - 1L)

        val results = listOf(
            async { useCase(1L, today) },
            async { useCase(1L, today) },
        ).awaitAll()

        val creditedSum = results.sum()
        assertEquals(1L, creditedSum, "total credited must equal the one headroom unit \u2014 no overflow")
        assertEquals(
            AwardBattleSteps.DAILY_BATTLE_STEP_CAP,
            dao.getBattleStepsEarned(today),
            "cap counter must advance by exactly 1, not 2",
        )
        assertEquals(1L, playerRepo.profile.value.stepBalance, "wallet must advance by exactly 1")
        assertEquals(2, dao.creditBattleStepsAtomicCallCount, "both calls reached the atomic path")
    }
}
