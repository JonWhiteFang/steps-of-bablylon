package com.whitefang.stepsofbabylon.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workshop_upgrade")
data class WorkshopUpgradeEntity(
    @PrimaryKey val upgradeType: String,
    val level: Int = 0,
)
