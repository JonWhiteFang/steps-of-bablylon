package com.whitefang.stepsofbabylon.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.whitefang.stepsofbabylon.data.sensor.DailyStepManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Periodic worker that catches up on missed steps if the foreground service was killed.
 * Reads the current TYPE_STEP_COUNTER value and computes delta since last sync.
 */
@HiltWorker
class StepSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val dailyStepManager: DailyStepManager,
    private val sensorManager: SensorManager,
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val PREFS_NAME = "step_sync"
        private const val KEY_LAST_COUNTER = "last_counter_value"
    }

    override suspend fun doWork(): Result {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return Result.success()

        // Read current cumulative counter via a one-shot trigger event
        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCounter = prefs.getLong(KEY_LAST_COUNTER, -1L)

        // Use triggerEventListener for a one-shot reading
        val currentCounter = readCurrentCounter(sensor) ?: return Result.retry()

        if (lastCounter >= 0) {
            val delta = currentCounter - lastCounter
            if (delta > 0) {
                dailyStepManager.recordSteps(delta, System.currentTimeMillis())
            }
        }

        prefs.edit().putLong(KEY_LAST_COUNTER, currentCounter).apply()
        return Result.success()
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
