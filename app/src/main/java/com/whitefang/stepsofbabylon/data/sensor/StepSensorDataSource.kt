package com.whitefang.stepsofbabylon.data.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps TYPE_STEP_COUNTER sensor and emits step deltas as a Flow.
 * The hardware sensor returns a cumulative count since last reboot;
 * this class computes deltas between readings.
 */
@Singleton
class StepSensorDataSource @Inject constructor(
    private val sensorManager: SensorManager,
) {
    companion object {
        private const val TAG = "StepSensorDataSource"
    }

    val stepDeltas: Flow<Long> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (sensor == null) {
            Log.w(TAG, "TYPE_STEP_COUNTER sensor not available")
            close()
            return@callbackFlow
        }

        var lastCumulative = -1L

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val cumulative = event.values[0].toLong()
                if (lastCumulative < 0) {
                    lastCumulative = cumulative
                    return // First reading — establish baseline
                }
                val delta = cumulative - lastCumulative
                lastCumulative = cumulative
                if (delta > 0) {
                    trySend(delta)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
