package com.whitefang.stepsofbabylon.fakes

import com.whitefang.stepsofbabylon.data.local.MilestoneDao
import com.whitefang.stepsofbabylon.data.local.MilestoneEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeMilestoneDao : MilestoneDao {
    private val data = MutableStateFlow<Map<String, MilestoneEntity>>(emptyMap())

    override fun getAll(): Flow<List<MilestoneEntity>> = data.map { it.values.toList() }

    override suspend fun getAllOnce(): List<MilestoneEntity> = data.value.values.toList()

    override suspend fun getByIdOnce(id: String): MilestoneEntity? = data.value[id]

    override suspend fun upsert(entity: MilestoneEntity) {
        data.value = data.value + (entity.milestoneId to entity)
    }
}
