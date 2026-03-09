package com.whitefang.stepsofbabylon.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "milestone")
data class MilestoneEntity(
    @PrimaryKey val milestoneId: String,
    val claimed: Boolean = false,
    val claimedAt: Long? = null,
)
