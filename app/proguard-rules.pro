# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }

# SQLCipher
-keep class net.zetetic.** { *; }

# Keep game domain models (enums stored as names in Room)
-keep enum com.whitefang.stepsofbabylon.domain.model.** { *; }
