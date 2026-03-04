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

    @Query("SELECT * FROM daily_step_record WHERE date = :date")
    suspend fun getByDateOnce(date: String): DailyStepRecordEntity?

    @Query("UPDATE daily_step_record SET escrowSteps = 0, escrowSyncCount = 0 WHERE date = :date")
    suspend fun clearEscrow(date: String)
}
