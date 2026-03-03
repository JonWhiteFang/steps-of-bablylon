package com.whitefang.stepsofbabylon.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStepDao {

    @Query("SELECT * FROM daily_step_record WHERE date = :date")
    fun getByDate(date: String): Flow<DailyStepRecordEntity?>

    @Query("SELECT * FROM daily_step_record WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getRange(startDate: String, endDate: String): Flow<List<DailyStepRecordEntity>>

    @Upsert
    suspend fun upsert(entity: DailyStepRecordEntity)
}
