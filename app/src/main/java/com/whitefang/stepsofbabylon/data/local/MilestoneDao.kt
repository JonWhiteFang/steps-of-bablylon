package com.whitefang.stepsofbabylon.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao {

    @Query("SELECT * FROM milestone")
    fun getAll(): Flow<List<MilestoneEntity>>

    @Query("SELECT * FROM milestone")
    suspend fun getAllOnce(): List<MilestoneEntity>

    @Query("SELECT * FROM milestone WHERE milestoneId = :id")
    suspend fun getByIdOnce(id: String): MilestoneEntity?

    @Upsert
    suspend fun upsert(entity: MilestoneEntity)
}
