package com.whitefang.stepsofbabylon.fakes

import com.whitefang.stepsofbabylon.domain.model.UpgradeCategory
import com.whitefang.stepsofbabylon.domain.model.UpgradeType
import com.whitefang.stepsofbabylon.domain.repository.WorkshopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeWorkshopRepository : WorkshopRepository {

    val upgrades = MutableStateFlow<Map<UpgradeType, Int>>(emptyMap())

    override fun observeAllUpgrades(): Flow<Map<UpgradeType, Int>> = upgrades
    override fun observeUpgradeLevel(type: UpgradeType): Flow<Int> = upgrades.map { it[type] ?: 0 }
    override fun observeUpgradesByCategory(category: UpgradeCategory): Flow<Map<UpgradeType, Int>> =
        upgrades.map { map -> map.filter { it.key.category == category } }

    override suspend fun setUpgradeLevel(type: UpgradeType, level: Int) {
        upgrades.update { it + (type to level) }
    }

    override suspend fun ensureUpgradesExist() {}
}
