package com.whitefang.stepsofbabylon.data.anticheat

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AntiCheatPreferences @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences("anti_cheat_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "AntiCheat"
        private const val KEY_COUNTER_DATE = "counter_date"
        private const val KEY_RATE_REJECTED = "rate_rejected"
        private const val KEY_VELOCITY_PENALIZED = "velocity_penalized"
        private const val KEY_ACTIVITY_REJECTED = "activity_rejected"
        private const val KEY_CV_OFFENSE_COUNT = "cv_offense_count"
        private const val KEY_CV_LAST_OFFENSE_DATE = "cv_last_offense_date"
        private const val DECAY_DAYS = 7L
    }

    fun resetDailyCounters(date: String) {
        val stored = prefs.getString(KEY_COUNTER_DATE, null)
        if (stored != date) {
            prefs.edit()
                .putString(KEY_COUNTER_DATE, date)
                .putLong(KEY_RATE_REJECTED, 0)
                .putLong(KEY_VELOCITY_PENALIZED, 0)
                .putLong(KEY_ACTIVITY_REJECTED, 0)
                .apply()
        }
    }

    fun incrementRateRejected(steps: Long) {
        val current = prefs.getLong(KEY_RATE_REJECTED, 0)
        prefs.edit().putLong(KEY_RATE_REJECTED, current + steps).apply()
        Log.d(TAG, "Rate-limited $steps steps (total today: ${current + steps})")
    }

    fun incrementVelocityPenalized(steps: Long) {
        val current = prefs.getLong(KEY_VELOCITY_PENALIZED, 0)
        prefs.edit().putLong(KEY_VELOCITY_PENALIZED, current + steps).apply()
        Log.d(TAG, "Velocity-penalized $steps steps (total today: ${current + steps})")
    }

    fun incrementActivityMinutesRejected(minutes: Long) {
        val current = prefs.getLong(KEY_ACTIVITY_REJECTED, 0)
        prefs.edit().putLong(KEY_ACTIVITY_REJECTED, current + minutes).apply()
        Log.d(TAG, "Rejected $minutes activity minutes (total today: ${current + minutes})")
    }

    fun recordCvOffense(date: String) {
        val count = prefs.getInt(KEY_CV_OFFENSE_COUNT, 0) + 1
        prefs.edit()
            .putInt(KEY_CV_OFFENSE_COUNT, count)
            .putString(KEY_CV_LAST_OFFENSE_DATE, date)
            .apply()
        Log.d(TAG, "CV offense recorded (total: $count)")
    }

    fun getCvOffenseCount(): Int = prefs.getInt(KEY_CV_OFFENSE_COUNT, 0)

    fun decayCvOffenses() {
        val count = prefs.getInt(KEY_CV_OFFENSE_COUNT, 0)
        if (count <= 0) return
        val lastDate = prefs.getString(KEY_CV_LAST_OFFENSE_DATE, null) ?: return
        try {
            val last = LocalDate.parse(lastDate, DateTimeFormatter.ISO_LOCAL_DATE)
            val daysSince = ChronoUnit.DAYS.between(last, LocalDate.now())
            if (daysSince >= DECAY_DAYS) {
                val newCount = (count - 1).coerceAtLeast(0)
                prefs.edit().putInt(KEY_CV_OFFENSE_COUNT, newCount).apply()
                Log.d(TAG, "CV offense decayed ($count → $newCount)")
            }
        } catch (_: Exception) { /* malformed date — leave as-is */ }
    }
}
