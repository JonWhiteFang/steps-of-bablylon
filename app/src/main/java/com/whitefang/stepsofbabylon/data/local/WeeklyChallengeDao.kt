package com.whitefang.stepsofbabylon.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface WeeklyChallengeDao {

    @Query("SELECT * FROM weekly_challenge WHERE weekStartDate = :weekStart")
    suspend fun getByWeek(weekStart: String): WeeklyChallengeEntity?

    @Upsert
    suspend fun upsert(entity: WeeklyChallengeEntity)
}
