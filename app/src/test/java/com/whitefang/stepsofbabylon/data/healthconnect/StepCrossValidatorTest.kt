package com.whitefang.stepsofbabylon.data.healthconnect

import com.whitefang.stepsofbabylon.data.anticheat.AntiCheatPreferences
import com.whitefang.stepsofbabylon.domain.model.DailyStepSummary
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository
import com.whitefang.stepsofbabylon.domain.repository.StepRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class StepCrossValidatorTest {

    private val stepReader: HealthConnectStepReader = mock()
    private val stepRepository: StepRepository = mock()
    private val playerRepository: PlayerRepository = mock()
    private val antiCheatPrefs: AntiCheatPreferences = mock()

    private val validator = StepCrossValidator(stepReader, stepRepository, playerRepository, antiCheatPrefs)

    private fun record(
        sensor: Long = 1000,
        hc: Long = 0,
        credited: Long = 1000,
        escrow: Long = 0,
        escrowSync: Int = 0,
    ) = DailyStepSummary(
        date = "2026-03-09",
        sensorSteps = sensor,
        healthConnectSteps = hc,
        creditedSteps = credited,
        escrowSteps = escrow,
        escrowSyncCount = escrowSync,
    )

    @Test
    fun `level 0 - discrepancy escrows excess`() = runTest {
        whenever(stepRepository.getDailyRecord("2026-03-09")).thenReturn(record(sensor = 1500, hc = 0))
        whenever(stepReader.getStepsForDate("2026-03-09")).thenReturn(1000L)
        whenever(antiCheatPrefs.getCvOffenseCount()).thenReturn(0)

        validator.validate("2026-03-09")

        verify(stepRepository).updateEscrow(eq("2026-03-09"), eq(500L), eq(1))
        verify(antiCheatPrefs).recordCvOffense("2026-03-09")
    }

    @Test
    fun `level 1 - faster discard after 2 syncs`() = runTest {
        whenever(stepRepository.getDailyRecord("2026-03-09")).thenReturn(
            record(sensor = 1500, hc = 0, escrow = 500, escrowSync = 1)
        )
        whenever(stepReader.getStepsForDate("2026-03-09")).thenReturn(1000L)
        whenever(antiCheatPrefs.getCvOffenseCount()).thenReturn(2)

        validator.validate("2026-03-09")

        verify(stepRepository).discardEscrow("2026-03-09")
    }

    @Test
    fun `level 2 - caps at HC value`() = runTest {
        whenever(stepRepository.getDailyRecord("2026-03-09")).thenReturn(
            record(sensor = 1500, credited = 1500)
        )
        whenever(stepReader.getStepsForDate("2026-03-09")).thenReturn(1000L)
        whenever(antiCheatPrefs.getCvOffenseCount()).thenReturn(4)

        validator.validate("2026-03-09")

        // credited (1500) > hcSteps (1000), so excess = 500 escrowed
        verify(stepRepository).updateEscrow(eq("2026-03-09"), eq(500L), eq(3))
    }

    @Test
    fun `level 3 - caps at HC minus 10 percent`() = runTest {
        whenever(stepRepository.getDailyRecord("2026-03-09")).thenReturn(
            record(sensor = 1500, credited = 1500)
        )
        whenever(stepReader.getStepsForDate("2026-03-09")).thenReturn(1000L)
        whenever(antiCheatPrefs.getCvOffenseCount()).thenReturn(7)

        validator.validate("2026-03-09")

        // capped = 1000 * 0.9 = 900, excess = 1500 - 900 = 600
        verify(stepRepository).updateEscrow(eq("2026-03-09"), eq(600L), eq(3))
    }

    @Test
    fun `no discrepancy releases escrow and decays offenses`() = runTest {
        whenever(stepRepository.getDailyRecord("2026-03-09")).thenReturn(
            record(sensor = 1000, hc = 0, escrow = 200)
        )
        whenever(stepReader.getStepsForDate("2026-03-09")).thenReturn(1000L)
        whenever(antiCheatPrefs.getCvOffenseCount()).thenReturn(2)

        validator.validate("2026-03-09")

        verify(playerRepository).addSteps(200L)
        verify(stepRepository).releaseEscrow("2026-03-09")
        verify(antiCheatPrefs).decayCvOffenses()
        verify(antiCheatPrefs, never()).recordCvOffense(any())
    }
}
