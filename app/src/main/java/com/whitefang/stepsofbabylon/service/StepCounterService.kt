package com.whitefang.stepsofbabylon.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import com.whitefang.stepsofbabylon.data.sensor.DailyStepManager
import com.whitefang.stepsofbabylon.data.sensor.StepIngestionPreferences
import com.whitefang.stepsofbabylon.data.sensor.StepSensorDataSource
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class StepCounterService : Service() {

    @Inject lateinit var sensorDataSource: StepSensorDataSource
    @Inject lateinit var dailyStepManager: DailyStepManager
    @Inject lateinit var notificationManager: StepNotificationManager
    @Inject lateinit var playerRepository: PlayerRepository
    @Inject lateinit var stepIngestionPrefs: StepIngestionPreferences
    @Inject lateinit var sensorManager: SensorManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        val notification = notificationManager.buildNotification(0, 0)
        startForeground(
            StepNotificationManager.NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH,
        )

        // Establish day-start counter baseline on startup
        scope.launch(Dispatchers.IO) {
            initDayStartCounter()
        }

        scope.launch {
            sensorDataSource.stepDeltas.collect { delta ->
                val now = System.currentTimeMillis()
                dailyStepManager.recordSteps(delta, now)
                stepIngestionPrefs.updateServiceHeartbeat(now)

                val balance = try { playerRepository.observeProfile().first().stepBalance } catch (_: Exception) { 0L }
                notificationManager.updateNotification(
                    dailySteps = dailyStepManager.getDailyCredited(),
                    balance = balance,
                )
            }
        }
    }

    /**
     * Sets the day-start cumulative counter if not already set for today.
     * This ensures the worker has a valid baseline when it takes over after service death.
     */
    private fun initDayStartCounter() {
        val today = LocalDate.now().toString()
        if (stepIngestionPrefs.getCounterAtDayStart(today) != null) return

        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return
        var value: Long? = null
        val latch = CountDownLatch(1)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                value = event.values[0].toLong()
                sensorManager.unregisterListener(this)
                latch.countDown()
            }
            override fun onAccuracyChanged(s: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        latch.await(5, TimeUnit.SECONDS)
        sensorManager.unregisterListener(listener)

        value?.let { stepIngestionPrefs.setCounterAtDayStart(today, it) }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
