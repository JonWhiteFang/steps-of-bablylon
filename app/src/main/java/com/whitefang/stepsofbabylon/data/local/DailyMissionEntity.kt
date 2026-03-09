package com.whitefang.stepsofbabylon.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_mission")
data class DailyMissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val missionType: String,
    val target: Int,
    val progress: Int = 0,
    val rewardGems: Int = 0,
    val rewardPowerStones: Int = 0,
    val completed: Boolean = false,
    val claimed: Boolean = false,
)
