package com.whitefang.stepsofbabylon.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.whitefang.stepsofbabylon.data.healthconnect.ActivityMinuteConverter
import com.whitefang.stepsofbabylon.data.healthconnect.ActivityMinuteValidator
import com.whitefang.stepsofbabylon.data.healthconnect.ExerciseSessionReader
import com.whitefang.stepsofbabylon.data.healthconnect.StepCrossValidator
import com.whitefang.stepsofbabylon.data.healthconnect.StepGapFiller
import com.whitefang.stepsofbabylon.data.sensor.DailyStepManager
import com.whitefang.stepsofbabylon.data.sensor.StepIngestionPreferences
import com.whitefang.stepsofbabylon.domain.repository.StepRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

@HiltWorker
class StepSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val dailyStepManager: DailyStepManager,
    private val sensorManager: SensorManager,
    private val stepGapFiller: StepGapFiller,
    private val stepCrossValidator: StepCrossValidator,
    private val exerciseSessionReader: ExerciseSessionReader,
    private val activityMinuteConverter: ActivityMinuteConverter,
    private val activityMinuteValidator: ActivityMinuteValidator,
    private val smartReminderManager: SmartReminderManager,
    private val stepIngestionPrefs: StepIngestionPreferences,
    private val stepRepository: StepRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now().toString()

        // 1. Sensor catch-up (only when foreground service is not alive)
        sensorCatchUp(today)

        // 2. Health Connect operations (best-effort)
        try {
            stepGapFiller.fillGaps(today)
            stepCrossValidator.validate(today)

            val sessions = exerciseSessionReader.getSessionsForDate(today)
            val validSessions = activityMinuteValidator.validate(sessions)
            if (validSessions.isNotEmpty()) {
                val sensorStepsPerMinute = dailyStepManager.getSensorStepsPerMinute()
                val result = activityMinuteConverter.convert(validSessions, sensorStepsPerMinute)
                dailyStepManager.recordActivityMinutes(result.activityMinutes, result.stepEquivalents)
            }
        } catch (e: Exception) {
            android.util.Log.w("StepSyncWorker", "HC sync failed", e)
        }

        // Smart reminders
        try { smartReminderManager.checkAndNotify() } catch (e: Exception) {
            android.util.Log.w("StepSyncWorker", "Smart reminder failed", e)
        }

        return Result.success()
    }

    private suspend fun sensorCatchUp(today: String) {
        // Skip if the foreground service is alive and handling ingestion
        val now = System.currentTimeMillis()
        if (stepIngestionPrefs.isServiceAlive(now)) return

        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return
        val currentCounter = readCurrentCounter(sensor) ?: return

        // Get or establish the day-start baseline
        val dayStart = stepIngestionPrefs.getCounterAtDayStart(today)
        if (dayStart == null) {
            // First read today — establish baseline, no delta to credit
            stepIngestionPrefs.setCounterAtDayStart(today, currentCounter)
            return
        }

        // Compute how many raw sensor steps have occurred today
        val rawToday = currentCounter - dayStart
        if (rawToday <= 0) return

        // Check how many sensor steps have already been credited via Room
        val record = stepRepository.getDailyRecord(today)
        val alreadyCredited = record?.sensorSteps ?: 0L
        val gap = rawToday - alreadyCredited
        if (gap > 0) {
            dailyStepManager.recordSteps(gap, now)
        }
    }

    private fun readCurrentCounter(sensor: Sensor): Long? {
        var value: Long? = null
        val latch = java.util.concurrent.CountDownLatch(1)

        val listener = object : android.hardware.SensorEventListener {
            override fun onSensorChanged(event: android.hardware.SensorEvent) {
                value = event.values[0].toLong()
                sensorManager.unregisterListener(this)
                latch.countDown()
            }
            override fun onAccuracyChanged(s: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        latch.await(5, java.util.concurrent.TimeUnit.SECONDS)
        sensorManager.unregisterListener(listener)
        return value
    }
}
