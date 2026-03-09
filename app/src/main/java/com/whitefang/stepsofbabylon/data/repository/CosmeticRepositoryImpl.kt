package com.whitefang.stepsofbabylon.data.repository

import com.whitefang.stepsofbabylon.data.local.CosmeticDao
import com.whitefang.stepsofbabylon.data.local.CosmeticEntity
import com.whitefang.stepsofbabylon.domain.model.CosmeticCategory
import com.whitefang.stepsofbabylon.domain.model.CosmeticItem
import com.whitefang.stepsofbabylon.domain.repository.CosmeticRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CosmeticRepositoryImpl @Inject constructor(
    private val dao: CosmeticDao,
) : CosmeticRepository {

    override fun observeAll(): Flow<List<CosmeticItem>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeOwned(): Flow<List<CosmeticItem>> =
        dao.observeOwned().map { list -> list.map { it.toDomain() } }

    override fun observeEquipped(): Flow<List<CosmeticItem>> =
        dao.observeEquipped().map { list -> list.map { it.toDomain() } }

    override suspend fun purchase(cosmeticId: String) = dao.markOwned(cosmeticId)

    override suspend fun equip(cosmeticId: String) {
        val entities = dao.observeAll().first()
        val target = entities.find { it.cosmeticId == cosmeticId } ?: return
        dao.unequipCategory(target.category)
        dao.equip(cosmeticId)
    }

    override suspend fun unequip(cosmeticId: String) = dao.unequip(cosmeticId)

    override suspend fun ensureSeedData() {
        if (dao.count() > 0) return
        dao.upsertAll(SEED_COSMETICS)
    }

    private fun CosmeticEntity.toDomain() = CosmeticItem(
        cosmeticId = cosmeticId,
        category = CosmeticCategory.valueOf(category),
        name = name,
        description = description,
        priceGems = priceGems,
        isOwned = isOwned,
        isEquipped = isEquipped,
    )

    companion object {
        private val SEED_COSMETICS = listOf(
            CosmeticEntity(cosmeticId = "zig_obsidian", category = "ZIGGURAT_SKIN", name = "Obsidian Ziggurat", description = "Dark volcanic stone", priceGems = 100),
            CosmeticEntity(cosmeticId = "zig_crystal", category = "ZIGGURAT_SKIN", name = "Crystal Ziggurat", description = "Translucent crystal layers", priceGems = 200),
            CosmeticEntity(cosmeticId = "zig_golden", category = "ZIGGURAT_SKIN", name = "Golden Ziggurat", description = "Pure gold plating", priceGems = 300),
            CosmeticEntity(cosmeticId = "proj_fire", category = "PROJECTILE_EFFECT", name = "Fire Trails", description = "Blazing projectile trails", priceGems = 150),
            CosmeticEntity(cosmeticId = "proj_lightning", category = "PROJECTILE_EFFECT", name = "Lightning Arcs", description = "Electric projectile arcs", priceGems = 150),
            CosmeticEntity(cosmeticId = "enemy_shadow", category = "ENEMY_SKIN", name = "Shadow Enemies", description = "Dark silhouette enemies", priceGems = 100),
            CosmeticEntity(cosmeticId = "enemy_neon", category = "ENEMY_SKIN", name = "Neon Enemies", description = "Glowing neon outlines", priceGems = 100),
        )
    }
}
