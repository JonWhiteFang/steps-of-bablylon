package com.whitefang.stepsofbabylon.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.whitefang.stepsofbabylon.data.healthconnect.ActivityMinuteConverter
import com.whitefang.stepsofbabylon.data.healthconnect.ExerciseSessionReader
import com.whitefang.stepsofbabylon.data.healthconnect.StepCrossValidator
import com.whitefang.stepsofbabylon.data.healthconnect.StepGapFiller
import com.whitefang.stepsofbabylon.data.sensor.DailyStepManager
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
    private val smartReminderManager: SmartReminderManager,
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val PREFS_NAME = "step_sync"
        private const val KEY_LAST_COUNTER = "last_counter_value"
    }

    override suspend fun doWork(): Result {
        val today = LocalDate.now().toString()

        // 1. Sensor catch-up
        sensorCatchUp()

        // 2. Health Connect operations (best-effort)
        try {
            stepGapFiller.fillGaps(today)
            stepCrossValidator.validate(today)

            val sessions = exerciseSessionReader.getSessionsForDate(today)
            if (sessions.isNotEmpty()) {
                val result = activityMinuteConverter.convert(sessions, emptyMap())
                dailyStepManager.recordActivityMinutes(result.activityMinutes, result.stepEquivalents)
            }
        } catch (_: Exception) {
            // HC unavailable or error — skip gracefully
        }

        // Smart reminders
        try { smartReminderManager.checkAndNotify() } catch (_: Exception) {}

        return Result.success()
    }

    private suspend fun sensorCatchUp() {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return
        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCounter = prefs.getLong(KEY_LAST_COUNTER, -1L)
        val currentCounter = readCurrentCounter(sensor) ?: return

        if (lastCounter >= 0) {
            val delta = currentCounter - lastCounter
            if (delta > 0) {
                dailyStepManager.recordSteps(delta, System.currentTimeMillis())
            }
        }

        prefs.edit().putLong(KEY_LAST_COUNTER, currentCounter).apply()
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
