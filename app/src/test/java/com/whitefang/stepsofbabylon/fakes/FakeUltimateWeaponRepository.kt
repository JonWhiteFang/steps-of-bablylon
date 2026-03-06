package com.whitefang.stepsofbabylon.fakes

import com.whitefang.stepsofbabylon.domain.model.OwnedWeapon
import com.whitefang.stepsofbabylon.domain.model.UltimateWeaponType
import com.whitefang.stepsofbabylon.domain.repository.UltimateWeaponRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeUltimateWeaponRepository : UltimateWeaponRepository {
    val weapons = MutableStateFlow<Map<UltimateWeaponType, OwnedWeapon>>(emptyMap())

    override fun observeUnlockedWeapons(): Flow<List<OwnedWeapon>> = weapons.map { it.values.toList() }
    override fun observeEquippedWeapons(): Flow<List<OwnedWeapon>> = weapons.map { m -> m.values.filter { it.isEquipped } }

    override suspend fun unlockWeapon(type: UltimateWeaponType) {
        weapons.update { it + (type to OwnedWeapon(type, 1, false)) }
    }
    override suspend fun upgradeWeapon(type: UltimateWeaponType, newLevel: Int) {
        weapons.update { m -> m[type]?.let { m + (type to it.copy(level = newLevel)) } ?: m }
    }
    override suspend fun equipWeapon(type: UltimateWeaponType) {
        weapons.update { m -> m[type]?.let { m + (type to it.copy(isEquipped = true)) } ?: m }
    }
    override suspend fun unequipWeapon(type: UltimateWeaponType) {
        weapons.update { m -> m[type]?.let { m + (type to it.copy(isEquipped = false)) } ?: m }
    }
}
