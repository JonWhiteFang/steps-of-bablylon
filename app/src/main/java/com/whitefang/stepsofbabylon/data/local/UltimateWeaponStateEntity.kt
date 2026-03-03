package com.whitefang.stepsofbabylon.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ultimate_weapon_state")
data class UltimateWeaponStateEntity(
    @PrimaryKey val weaponType: String,
    val level: Int = 1,
    val isEquipped: Boolean = false,
)
