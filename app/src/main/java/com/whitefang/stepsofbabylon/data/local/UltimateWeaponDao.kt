package com.whitefang.stepsofbabylon.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UltimateWeaponDao {

    @Query("SELECT * FROM ultimate_weapon_state")
    fun getAll(): Flow<List<UltimateWeaponStateEntity>>

    @Query("SELECT * FROM ultimate_weapon_state WHERE isEquipped = 1")
    fun getEquipped(): Flow<List<UltimateWeaponStateEntity>>

    @Upsert
    suspend fun upsert(entity: UltimateWeaponStateEntity)

    @Query("SELECT COUNT(*) FROM ultimate_weapon_state WHERE isEquipped = 1")
    fun countEquipped(): Flow<Int>
}
