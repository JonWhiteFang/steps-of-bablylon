package com.whitefang.stepsofbabylon.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WalkingEncounterDao {

    @Query("SELECT * FROM walking_encounter WHERE claimed = 0 ORDER BY createdAt DESC")
    fun getUnclaimed(): Flow<List<WalkingEncounterEntity>>

    @Query("SELECT * FROM walking_encounter ORDER BY createdAt DESC LIMIT :limit")
    fun getHistory(limit: Int): Flow<List<WalkingEncounterEntity>>

    @Insert
    suspend fun insert(entity: WalkingEncounterEntity): Long

    @Query("UPDATE walking_encounter SET claimed = 1, claimedAt = :claimedAt WHERE id = :id")
    suspend fun markClaimed(id: Int, claimedAt: Long)

    @Query("SELECT COUNT(*) FROM walking_encounter WHERE claimed = 0")
    fun countUnclaimed(): Flow<Int>

    @Query("SELECT COUNT(*) FROM walking_encounter WHERE claimed = 0")
    suspend fun countUnclaimedOnce(): Int

    @Query("DELETE FROM walking_encounter WHERE claimed = 0 AND id = (SELECT id FROM walking_encounter WHERE claimed = 0 ORDER BY createdAt ASC LIMIT 1)")
    suspend fun deleteOldestUnclaimed()
}
