package com.whitefang.stepsofbabylon.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cosmetics")
data class CosmeticEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cosmeticId: String,
    val category: String,
    val name: String,
    val description: String = "",
    val priceGems: Long,
    val isOwned: Boolean = false,
    val isEquipped: Boolean = false,
)
