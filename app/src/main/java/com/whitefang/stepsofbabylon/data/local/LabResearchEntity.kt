package com.whitefang.stepsofbabylon.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lab_research")
data class LabResearchEntity(
    @PrimaryKey val researchType: String,
    val level: Int = 0,
    val startedAt: Long? = null,
    val completesAt: Long? = null,
)
