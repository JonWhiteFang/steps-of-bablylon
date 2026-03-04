package com.whitefang.stepsofbabylon.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.whitefang.stepsofbabylon.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepNotificationManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    companion object {
        const val CHANNEL_ID = "step_counter"
        const val NOTIFICATION_ID = 1001
        private const val THROTTLE_MS = 30_000L
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var lastUpdateMs = 0L

    init {
        val channel = NotificationChannel(
            CHANNEL_ID, "Step Counter", NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Shows your daily step count" }
        notificationManager.createNotificationChannel(channel)
    }

    fun buildNotification(dailySteps: Long, balance: Long): Notification {
        val tapIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setContentTitle("Steps of Babylon")
            .setContentText("Today: $dailySteps steps | Balance: $balance Steps")
            .setContentIntent(tapIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    fun updateNotification(dailySteps: Long, balance: Long) {
        val now = System.currentTimeMillis()
        if (now - lastUpdateMs < THROTTLE_MS) return
        lastUpdateMs = now
        notificationManager.notify(NOTIFICATION_ID, buildNotification(dailySteps, balance))
    }
}
