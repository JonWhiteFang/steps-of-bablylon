package com.whitefang.stepsofbabylon.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val totalStepsEarned: Long = 0,
    val currentStepBalance: Long = 0,
    val currentTier: Int = 1,
)
