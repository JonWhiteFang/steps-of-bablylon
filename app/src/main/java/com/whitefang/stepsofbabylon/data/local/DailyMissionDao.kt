package com.whitefang.stepsofbabylon.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyMissionDao {

    @Query("SELECT * FROM daily_mission WHERE date = :date")
    fun getByDate(date: String): Flow<List<DailyMissionEntity>>

    @Query("SELECT * FROM daily_mission WHERE date = :date")
    suspend fun getByDateOnce(date: String): List<DailyMissionEntity>

    @Insert
    suspend fun insert(entity: DailyMissionEntity)

    @Upsert
    suspend fun upsert(entity: DailyMissionEntity)

    @Query("UPDATE daily_mission SET progress = :progress, completed = :completed WHERE id = :id")
    suspend fun updateProgress(id: Int, progress: Int, completed: Boolean)

    @Query("UPDATE daily_mission SET claimed = 1 WHERE id = :id")
    suspend fun markClaimed(id: Int)

    @Query("SELECT COUNT(*) FROM daily_mission WHERE date = :date AND completed = 1 AND claimed = 0")
    fun countClaimable(date: String): Flow<Int>
}
