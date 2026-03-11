package com.whitefang.stepsofbabylon.data.sensor

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared state for coordinating step ingestion between StepCounterService and StepSyncWorker.
 * Prevents double-crediting by providing a service heartbeat and day-start counter baseline.
 */
@Singleton
class StepIngestionPreferences @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences("step_ingestion", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SERVICE_HEARTBEAT = "service_heartbeat"
        private const val KEY_DAY_START_COUNTER = "day_start_counter"
        private const val KEY_DAY_START_DATE = "day_start_date"
        const val HEARTBEAT_THRESHOLD_MS = 2 * 60 * 1000L // 2 minutes
    }

    fun updateServiceHeartbeat(timestampMs: Long) {
        prefs.edit().putLong(KEY_SERVICE_HEARTBEAT, timestampMs).apply()
    }

    fun getServiceHeartbeat(): Long = prefs.getLong(KEY_SERVICE_HEARTBEAT, 0L)

    fun isServiceAlive(nowMs: Long): Boolean =
        nowMs - getServiceHeartbeat() < HEARTBEAT_THRESHOLD_MS

    fun setCounterAtDayStart(date: String, counterValue: Long) {
        prefs.edit()
            .putString(KEY_DAY_START_DATE, date)
            .putLong(KEY_DAY_START_COUNTER, counterValue)
            .apply()
    }

    fun getCounterAtDayStart(date: String): Long? {
        val storedDate = prefs.getString(KEY_DAY_START_DATE, null)
        if (storedDate != date) return null
        return prefs.getLong(KEY_DAY_START_COUNTER, -1L).takeIf { it >= 0 }
    }
}
