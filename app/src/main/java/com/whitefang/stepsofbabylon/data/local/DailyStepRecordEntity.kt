package com.whitefang.stepsofbabylon.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_step_record")
data class DailyStepRecordEntity(
    @PrimaryKey val date: String, // ISO yyyy-MM-dd
    val sensorSteps: Long = 0,
    val healthConnectSteps: Long = 0,
    val creditedSteps: Long = 0,
    val escrowSteps: Long = 0,
    val escrowSyncCount: Int = 0,
    val activityMinutes: Map<String, Int> = emptyMap(),
    val stepEquivalents: Long = 0,
)
