package com.whitefang.stepsofbabylon.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "walking_encounter")
data class WalkingEncounterEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val triggerType: String,
    val rewardType: String,
    val rewardAmount: Int,
    val claimed: Boolean = false,
    val createdAt: Long,
    val claimedAt: Long? = null,
)
