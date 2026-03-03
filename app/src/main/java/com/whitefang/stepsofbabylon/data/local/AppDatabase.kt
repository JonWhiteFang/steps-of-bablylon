package com.whitefang.stepsofbabylon.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        PlayerProfileEntity::class,
        WorkshopUpgradeEntity::class,
        LabResearchEntity::class,
        CardInventoryEntity::class,
        UltimateWeaponStateEntity::class,
        DailyStepRecordEntity::class,
        WalkingEncounterEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerProfileDao(): PlayerProfileDao
    abstract fun workshopDao(): WorkshopDao
    abstract fun labDao(): LabDao
    abstract fun cardDao(): CardDao
    abstract fun ultimateWeaponDao(): UltimateWeaponDao
    abstract fun dailyStepDao(): DailyStepDao
    abstract fun walkingEncounterDao(): WalkingEncounterDao
}
