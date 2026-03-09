package com.whitefang.stepsofbabylon.data.healthconnect

import com.whitefang.stepsofbabylon.data.anticheat.AntiCheatPreferences
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository
import com.whitefang.stepsofbabylon.domain.repository.StepRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Compares sensor steps vs Health Connect steps with graduated response.
 * Offense history tracked across days — repeat offenders face escalating penalties.
 *
 * Level 0 (0 offenses): escrow excess, release on reconciliation (3 syncs)
 * Level 1 (1–2 offenses): escrow with faster discard (2 syncs)
 * Level 2 (3–5 offenses): cap credited at HC value
 * Level 3 (6+ offenses): cap at HC value minus 10% penalty
 */
@Singleton
class StepCrossValidator @Inject constructor(
    private val stepReader: HealthConnectStepReader,
    private val stepRepository: StepRepository,
    private val playerRepository: PlayerRepository,
    private val antiCheatPrefs: AntiCheatPreferences,
) {
    companion object {
        private const val DISCREPANCY_THRESHOLD = 0.20
        private const val MAX_ESCROW_SYNCS_DEFAULT = 3
        private const val MAX_ESCROW_SYNCS_LEVEL1 = 2
        private const val LEVEL2_PENALTY = 0.0
        private const val LEVEL3_PENALTY = 0.10
    }

    suspend fun validate(date: String) {
        val record = stepRepository.getDailyRecord(date) ?: return
        val hcSteps = stepReader.getStepsForDate(date) ?: return

        stepRepository.updateHealthConnectSteps(date, hcSteps)

        val sensorSteps = record.sensorSteps
        if (sensorSteps <= 0 || hcSteps <= 0) return

        val discrepancy = (sensorSteps - hcSteps).toDouble() / hcSteps
        val offenseCount = antiCheatPrefs.getCvOffenseCount()

        if (discrepancy > DISCREPANCY_THRESHOLD) {
            antiCheatPrefs.recordCvOffense(date)

            when {
                // Level 3: cap at HC minus penalty
                offenseCount >= 6 -> {
                    val capped = (hcSteps * (1.0 - LEVEL3_PENALTY)).toLong()
                    if (record.creditedSteps > capped) {
                        val excess = record.creditedSteps - capped
                        stepRepository.updateEscrow(date, excess, MAX_ESCROW_SYNCS_DEFAULT)
                    }
                }
                // Level 2: cap at HC value
                offenseCount >= 3 -> {
                    if (record.creditedSteps > hcSteps) {
                        val excess = record.creditedSteps - hcSteps
                        stepRepository.updateEscrow(date, excess, MAX_ESCROW_SYNCS_DEFAULT)
                    }
                }
                // Level 1: escrow with faster discard
                offenseCount >= 1 -> {
                    val excess = sensorSteps - hcSteps
                    val newSyncCount = record.escrowSyncCount + 1
                    if (newSyncCount >= MAX_ESCROW_SYNCS_LEVEL1 && record.escrowSteps > 0) {
                        stepRepository.discardEscrow(date)
                    } else {
                        stepRepository.updateEscrow(date, excess, newSyncCount)
                    }
                }
                // Level 0: default behavior
                else -> {
                    val excess = sensorSteps - hcSteps
                    val newSyncCount = record.escrowSyncCount + 1
                    if (newSyncCount >= MAX_ESCROW_SYNCS_DEFAULT && record.escrowSteps > 0) {
                        stepRepository.discardEscrow(date)
                    } else {
                        stepRepository.updateEscrow(date, excess, newSyncCount)
                    }
                }
            }
        } else if (record.escrowSteps > 0) {
            // Discrepancy resolved — release escrow and decay offenses
            playerRepository.addSteps(record.escrowSteps)
            stepRepository.releaseEscrow(date)
            antiCheatPrefs.decayCvOffenses()
        }
    }
}
