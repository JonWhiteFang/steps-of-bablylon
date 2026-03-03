package com.whitefang.stepsofbabylon.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "card_inventory")
data class CardInventoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cardType: String,
    val level: Int = 1,
    val isEquipped: Boolean = false,
)
