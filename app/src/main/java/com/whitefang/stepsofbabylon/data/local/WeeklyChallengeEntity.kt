package com.whitefang.stepsofbabylon.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weekly_challenge")
data class WeeklyChallengeEntity(
    @PrimaryKey val weekStartDate: String,
    val totalSteps: Long = 0,
    val claimedTier: Int = 0,
)
