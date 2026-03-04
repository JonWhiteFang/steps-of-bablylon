package com.whitefang.stepsofbabylon.data.healthconnect

import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository
import com.whitefang.stepsofbabylon.domain.repository.StepRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Compares sensor steps vs Health Connect steps.
 * If discrepancy >20%, excess goes to escrow.
 * Escrow releases after reconciliation or discards after 3 failed syncs.
 */
@Singleton
class StepCrossValidator @Inject constructor(
    private val stepReader: HealthConnectStepReader,
    private val stepRepository: StepRepository,
    private val playerRepository: PlayerRepository,
) {
    companion object {
        private const val DISCREPANCY_THRESHOLD = 0.20
        private const val MAX_ESCROW_SYNCS = 3
    }

    suspend fun validate(date: String) {
        val record = stepRepository.getDailyRecord(date) ?: return
        val hcSteps = stepReader.getStepsForDate(date) ?: return

        stepRepository.updateHealthConnectSteps(date, hcSteps)

        val sensorSteps = record.sensorSteps
        if (sensorSteps <= 0 || hcSteps <= 0) return

        val discrepancy = (sensorSteps - hcSteps).toDouble() / hcSteps

        if (discrepancy > DISCREPANCY_THRESHOLD) {
            // Sensor reports significantly more than HC — suspicious
            val excess = sensorSteps - hcSteps
            val newSyncCount = record.escrowSyncCount + 1

            if (newSyncCount >= MAX_ESCROW_SYNCS && record.escrowSteps > 0) {
                // Too many failed syncs — discard escrow
                stepRepository.discardEscrow(date)
            } else {
                stepRepository.updateEscrow(date, excess, newSyncCount)
            }
        } else if (record.escrowSteps > 0) {
            // Discrepancy resolved — release escrow to player balance
            playerRepository.addSteps(record.escrowSteps)
            stepRepository.releaseEscrow(date)
        }
    }
}
