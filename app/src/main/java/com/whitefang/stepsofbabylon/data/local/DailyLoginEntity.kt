package com.whitefang.stepsofbabylon.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_login")
data class DailyLoginEntity(
    @PrimaryKey val date: String,
    val stepsWalked: Long = 0,
    val powerStoneClaimed: Boolean = false,
    val gemsClaimed: Boolean = false,
)
