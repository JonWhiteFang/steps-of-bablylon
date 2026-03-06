package com.whitefang.stepsofbabylon.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val totalStepsEarned: Long = 0,
    val currentStepBalance: Long = 0,
    val gems: Long = 0,
    val powerStones: Long = 0,
    val cardDust: Long = 0,
    val currentTier: Int = 1,
    @ColumnInfo(defaultValue = "1")
    val highestUnlockedTier: Int = 1,
    val bestWavePerTier: Map<Int, Int> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis(),
)
