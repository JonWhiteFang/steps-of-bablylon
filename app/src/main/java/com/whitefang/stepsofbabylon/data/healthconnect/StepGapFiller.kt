package com.whitefang.stepsofbabylon.data.healthconnect

import com.whitefang.stepsofbabylon.data.sensor.DailyStepManager
import com.whitefang.stepsofbabylon.domain.repository.StepRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Recovers missed steps from Health Connect when the foreground service was killed.
 * If HC reports more steps than our sensor recorded, the difference is the gap.
 */
@Singleton
class StepGapFiller @Inject constructor(
    private val stepReader: HealthConnectStepReader,
    private val dailyStepManager: DailyStepManager,
    private val stepRepository: StepRepository,
) {
    suspend fun fillGaps(date: String) {
        val record = stepRepository.getDailyRecord(date)
        val sensorTotal = record?.sensorSteps ?: 0
        val hcTotal = stepReader.getStepsForDate(date) ?: return

        val gap = hcTotal - sensorTotal
        if (gap > 0) {
            dailyStepManager.recordSteps(gap, System.currentTimeMillis())
        }
    }
}
