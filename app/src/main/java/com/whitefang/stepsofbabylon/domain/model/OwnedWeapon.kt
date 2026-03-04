package com.whitefang.stepsofbabylon.domain.model

data class OwnedWeapon(
    val type: UltimateWeaponType,
    val level: Int,
    val isEquipped: Boolean,
)
