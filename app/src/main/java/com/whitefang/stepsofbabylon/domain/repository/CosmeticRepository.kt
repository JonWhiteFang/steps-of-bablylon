package com.whitefang.stepsofbabylon.domain.repository

import com.whitefang.stepsofbabylon.domain.model.CosmeticItem
import kotlinx.coroutines.flow.Flow

interface CosmeticRepository {
    fun observeAll(): Flow<List<CosmeticItem>>
    fun observeOwned(): Flow<List<CosmeticItem>>
    fun observeEquipped(): Flow<List<CosmeticItem>>
    suspend fun purchase(cosmeticId: String)
    suspend fun equip(cosmeticId: String)
    suspend fun unequip(cosmeticId: String)
    suspend fun ensureSeedData()
}
