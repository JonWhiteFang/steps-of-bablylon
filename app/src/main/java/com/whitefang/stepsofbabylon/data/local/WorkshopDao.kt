package com.whitefang.stepsofbabylon.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkshopDao {

    @Query("SELECT * FROM workshop_upgrade")
    fun getAll(): Flow<List<WorkshopUpgradeEntity>>

    @Query("SELECT * FROM workshop_upgrade WHERE upgradeType = :upgradeType")
    fun getByType(upgradeType: String): Flow<WorkshopUpgradeEntity?>

    @Query("SELECT * FROM workshop_upgrade WHERE upgradeType IN (:types)")
    fun getByCategory(types: List<String>): Flow<List<WorkshopUpgradeEntity>>

    @Upsert
    suspend fun upsert(entity: WorkshopUpgradeEntity)

    @Upsert
    suspend fun upsertAll(entities: List<WorkshopUpgradeEntity>)
}
