# ============================================================
# Steps of Babylon — R8 / ProGuard Rules
# ============================================================

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-dontwarn androidx.room.paging.**

# --- Hilt / Dagger ---
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }

# --- WorkManager + Hilt Worker Factory ---
-keep class * extends androidx.work.ListenableWorker { *; }

# --- SQLCipher ---
-keep class net.zetetic.** { *; }

# --- Health Connect SDK (uses reflection internally) ---
-keep class androidx.health.connect.** { *; }

# --- Sensor callbacks (invoked by framework via reflection) ---
-keep class * implements android.hardware.SensorEventListener {
    void onSensorChanged(android.hardware.SensorEvent);
    void onAccuracyChanged(android.hardware.Sensor, int);
}

# --- Game domain models (enums stored as names in Room) ---
-keep enum com.whitefang.stepsofbabylon.domain.model.** { *; }

# --- Room TypeConverters (uses org.json — Android framework, defensive keep) ---
-keep class org.json.** { *; }
