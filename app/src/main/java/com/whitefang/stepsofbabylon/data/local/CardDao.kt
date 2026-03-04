package com.whitefang.stepsofbabylon.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    @Query("SELECT * FROM card_inventory")
    fun getAll(): Flow<List<CardInventoryEntity>>

    @Query("SELECT * FROM card_inventory WHERE isEquipped = 1")
    fun getEquipped(): Flow<List<CardInventoryEntity>>

    @Insert
    suspend fun insert(entity: CardInventoryEntity): Long

    @Update
    suspend fun update(entity: CardInventoryEntity)

    @Delete
    suspend fun delete(entity: CardInventoryEntity)

    @Query("SELECT COUNT(*) FROM card_inventory WHERE isEquipped = 1")
    fun countEquipped(): Flow<Int>

    @Query("SELECT * FROM card_inventory WHERE id = :id")
    suspend fun getById(id: Int): CardInventoryEntity?
}
