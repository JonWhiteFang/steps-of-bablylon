package com.whitefang.stepsofbabylon.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.whitefang.stepsofbabylon.data.NotificationPreferences
import com.whitefang.stepsofbabylon.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MilestoneNotificationManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val notificationPreferences: NotificationPreferences,
) {
    companion object {
        private const val CHANNEL_ID = "milestones"
        private const val WAVE_NOTIFICATION_ID = 4001
        private const val MILESTONE_NOTIFICATION_ID = 4002
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        val channel = NotificationChannel(CHANNEL_ID, "Milestones", NotificationManager.IMPORTANCE_DEFAULT)
            .apply { description = "Wave records and walking milestones" }
        notificationManager.createNotificationChannel(channel)
    }

    fun notifyNewBestWave(wave: Int, biomeName: String) {
        if (!notificationPreferences.isMilestoneAlertsEnabled()) return
        val intent = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("New Personal Best!")
            .setContentText("Wave $wave in $biomeName!")
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(WAVE_NOTIFICATION_ID, notification)
    }

    fun notifyMilestoneAchieved(milestoneName: String) {
        if (!notificationPreferences.isMilestoneAlertsEnabled()) return
        val intent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).putExtra("navigate_to", "missions"),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("$milestoneName!")
            .setContentText("Claim your reward!")
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(MILESTONE_NOTIFICATION_ID, notification)
    }
}
