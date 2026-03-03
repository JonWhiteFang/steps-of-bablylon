package com.whitefang.stepsofbabylon.di

import android.content.Context
import androidx.room.Room
import com.whitefang.stepsofbabylon.data.local.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "steps_of_babylon.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides fun providePlayerProfileDao(db: AppDatabase): PlayerProfileDao = db.playerProfileDao()
    @Provides fun provideWorkshopDao(db: AppDatabase): WorkshopDao = db.workshopDao()
    @Provides fun provideLabDao(db: AppDatabase): LabDao = db.labDao()
    @Provides fun provideCardDao(db: AppDatabase): CardDao = db.cardDao()
    @Provides fun provideUltimateWeaponDao(db: AppDatabase): UltimateWeaponDao = db.ultimateWeaponDao()
    @Provides fun provideDailyStepDao(db: AppDatabase): DailyStepDao = db.dailyStepDao()
    @Provides fun provideWalkingEncounterDao(db: AppDatabase): WalkingEncounterDao = db.walkingEncounterDao()
}
