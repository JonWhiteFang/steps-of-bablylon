package com.whitefang.stepsofbabylon.domain.repository

import com.whitefang.stepsofbabylon.domain.model.UpgradeCategory
import com.whitefang.stepsofbabylon.domain.model.UpgradeType
import kotlinx.coroutines.flow.Flow

interface WorkshopRepository {
    fun observeAllUpgrades(): Flow<Map<UpgradeType, Int>>
    fun observeUpgradeLevel(type: UpgradeType): Flow<Int>
    fun observeUpgradesByCategory(category: UpgradeCategory): Flow<Map<UpgradeType, Int>>
    suspend fun setUpgradeLevel(type: UpgradeType, level: Int)
    suspend fun ensureUpgradesExist()
}
