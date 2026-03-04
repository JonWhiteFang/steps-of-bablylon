package com.whitefang.stepsofbabylon.data.repository

import com.whitefang.stepsofbabylon.data.local.UltimateWeaponDao
import com.whitefang.stepsofbabylon.data.local.UltimateWeaponStateEntity
import com.whitefang.stepsofbabylon.domain.model.OwnedWeapon
import com.whitefang.stepsofbabylon.domain.model.UltimateWeaponType
import com.whitefang.stepsofbabylon.domain.repository.UltimateWeaponRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UltimateWeaponRepositoryImpl @Inject constructor(
    private val dao: UltimateWeaponDao,
) : UltimateWeaponRepository {

    override fun observeUnlockedWeapons(): Flow<List<OwnedWeapon>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun observeEquippedWeapons(): Flow<List<OwnedWeapon>> =
        dao.getEquipped().map { list -> list.map { it.toDomain() } }

    override suspend fun unlockWeapon(type: UltimateWeaponType) =
        dao.upsert(UltimateWeaponStateEntity(weaponType = type.name))

    override suspend fun upgradeWeapon(type: UltimateWeaponType, newLevel: Int) {
        val entity = dao.getByType(type.name) ?: return
        dao.upsert(entity.copy(level = newLevel))
    }

    override suspend fun equipWeapon(type: UltimateWeaponType) {
        val entity = dao.getByType(type.name) ?: return
        dao.upsert(entity.copy(isEquipped = true))
    }

    override suspend fun unequipWeapon(type: UltimateWeaponType) {
        val entity = dao.getByType(type.name) ?: return
        dao.upsert(entity.copy(isEquipped = false))
    }

    private fun UltimateWeaponStateEntity.toDomain() = OwnedWeapon(
        type = UltimateWeaponType.valueOf(weaponType),
        level = level,
        isEquipped = isEquipped,
    )
}
