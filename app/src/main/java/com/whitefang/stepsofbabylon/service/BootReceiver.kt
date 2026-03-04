package com.whitefang.stepsofbabylon.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val hasPermission = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            context.startForegroundService(
                Intent(context, StepCounterService::class.java)
            )
        }
    }
}
