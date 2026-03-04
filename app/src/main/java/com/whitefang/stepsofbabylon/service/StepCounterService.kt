package com.whitefang.stepsofbabylon.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import com.whitefang.stepsofbabylon.data.sensor.DailyStepManager
import com.whitefang.stepsofbabylon.data.sensor.StepSensorDataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StepCounterService : Service() {

    @Inject lateinit var sensorDataSource: StepSensorDataSource
    @Inject lateinit var dailyStepManager: DailyStepManager
    @Inject lateinit var notificationManager: StepNotificationManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        val notification = notificationManager.buildNotification(0, 0)
        startForeground(
            StepNotificationManager.NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH,
        )

        scope.launch {
            sensorDataSource.stepDeltas.collect { delta ->
                val now = System.currentTimeMillis()
                dailyStepManager.recordSteps(delta, now)
                notificationManager.updateNotification(
                    dailySteps = dailyStepManager.getDailyCredited(),
                    balance = 0, // Updated via notification refresh from repo if needed
                )
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
