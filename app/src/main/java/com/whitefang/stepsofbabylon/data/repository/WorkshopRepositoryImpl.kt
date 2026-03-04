package com.whitefang.stepsofbabylon.data.repository

import com.whitefang.stepsofbabylon.data.local.WorkshopDao
import com.whitefang.stepsofbabylon.data.local.WorkshopUpgradeEntity
import com.whitefang.stepsofbabylon.domain.model.UpgradeCategory
import com.whitefang.stepsofbabylon.domain.model.UpgradeType
import com.whitefang.stepsofbabylon.domain.repository.WorkshopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorkshopRepositoryImpl @Inject constructor(
    private val dao: WorkshopDao,
) : WorkshopRepository {

    override fun observeAllUpgrades(): Flow<Map<UpgradeType, Int>> =
        dao.getAll().map { list ->
            list.associate { UpgradeType.valueOf(it.upgradeType) to it.level }
        }

    override fun observeUpgradeLevel(type: UpgradeType): Flow<Int> =
        dao.getByType(type.name).map { it?.level ?: 0 }

    override fun observeUpgradesByCategory(category: UpgradeCategory): Flow<Map<UpgradeType, Int>> =
        dao.getByCategory(UpgradeType.entries.filter { it.category == category }.map { it.name })
            .map { list -> list.associate { UpgradeType.valueOf(it.upgradeType) to it.level } }

    override suspend fun setUpgradeLevel(type: UpgradeType, level: Int) =
        dao.upsert(WorkshopUpgradeEntity(upgradeType = type.name, level = level))

    override suspend fun ensureUpgradesExist() {
        if (dao.getAll().first().isEmpty()) {
            dao.upsertAll(UpgradeType.entries.map { WorkshopUpgradeEntity(upgradeType = it.name) })
        }
    }
}
