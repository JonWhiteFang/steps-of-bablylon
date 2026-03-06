package com.whitefang.stepsofbabylon.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.whitefang.stepsofbabylon.domain.model.SupplyDrop
import com.whitefang.stepsofbabylon.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupplyDropNotificationManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    companion object {
        const val CHANNEL_ID = "supply_drops"
        private const val BASE_NOTIFICATION_ID = 2000
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        val channel = NotificationChannel(
            CHANNEL_ID, "Supply Drops", NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Rewards earned while walking" }
        notificationManager.createNotificationChannel(channel)
    }

    fun notify(drop: SupplyDrop) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "supplies")
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context, drop.id, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("Supply Drop!")
            .setContentText(drop.trigger.message)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(BASE_NOTIFICATION_ID + drop.id, notification)
    }
}
