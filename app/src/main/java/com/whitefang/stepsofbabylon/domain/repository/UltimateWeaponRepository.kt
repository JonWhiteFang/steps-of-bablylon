package com.whitefang.stepsofbabylon.domain.repository

import com.whitefang.stepsofbabylon.domain.model.OwnedWeapon
import com.whitefang.stepsofbabylon.domain.model.UltimateWeaponType
import kotlinx.coroutines.flow.Flow

interface UltimateWeaponRepository {
    fun observeUnlockedWeapons(): Flow<List<OwnedWeapon>>
    fun observeEquippedWeapons(): Flow<List<OwnedWeapon>>
    suspend fun unlockWeapon(type: UltimateWeaponType)
    suspend fun upgradeWeapon(type: UltimateWeaponType, newLevel: Int)
    suspend fun equipWeapon(type: UltimateWeaponType)
    suspend fun unequipWeapon(type: UltimateWeaponType)
}
