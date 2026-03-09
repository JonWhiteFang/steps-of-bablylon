package com.whitefang.stepsofbabylon.fakes

import com.whitefang.stepsofbabylon.data.local.DailyMissionDao
import com.whitefang.stepsofbabylon.data.local.DailyMissionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeDailyMissionDao : DailyMissionDao {
    private val data = MutableStateFlow<List<DailyMissionEntity>>(emptyList())
    private var nextId = 1

    override fun getByDate(date: String): Flow<List<DailyMissionEntity>> =
        data.map { list -> list.filter { it.date == date } }

    override suspend fun getByDateOnce(date: String): List<DailyMissionEntity> =
        data.value.filter { it.date == date }

    override suspend fun insert(entity: DailyMissionEntity) {
        val withId = entity.copy(id = nextId++)
        data.value = data.value + withId
    }

    override suspend fun upsert(entity: DailyMissionEntity) {
        data.value = data.value.filter { it.id != entity.id } + entity
    }

    override suspend fun updateProgress(id: Int, progress: Int, completed: Boolean) {
        data.value = data.value.map {
            if (it.id == id) it.copy(progress = progress, completed = completed) else it
        }
    }

    override suspend fun markClaimed(id: Int) {
        data.value = data.value.map {
            if (it.id == id) it.copy(claimed = true) else it
        }
    }

    override fun countClaimable(date: String): Flow<Int> =
        data.map { list -> list.count { it.date == date && it.completed && !it.claimed } }
}
