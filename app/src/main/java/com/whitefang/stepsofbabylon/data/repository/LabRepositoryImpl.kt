package com.whitefang.stepsofbabylon.data.repository

import com.whitefang.stepsofbabylon.data.local.LabDao
import com.whitefang.stepsofbabylon.data.local.LabResearchEntity
import com.whitefang.stepsofbabylon.domain.model.ActiveResearch
import com.whitefang.stepsofbabylon.domain.model.ResearchType
import com.whitefang.stepsofbabylon.domain.repository.LabRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LabRepositoryImpl @Inject constructor(
    private val dao: LabDao,
) : LabRepository {

    override fun observeAllResearch(): Flow<Map<ResearchType, Int>> =
        dao.getAll().map { list ->
            list.associate { ResearchType.valueOf(it.researchType) to it.level }
        }

    override fun observeActiveResearch(): Flow<List<ActiveResearch>> =
        dao.getActive().map { list ->
            list.map { ActiveResearch(
                type = ResearchType.valueOf(it.researchType),
                level = it.level,
                startedAt = it.startedAt!!,
                completesAt = it.completesAt!!,
            ) }
        }

    override suspend fun startResearch(type: ResearchType, completesAt: Long) {
        val entity = dao.getByType(type.name).first() ?: return
        dao.upsert(entity.copy(startedAt = System.currentTimeMillis(), completesAt = completesAt))
    }

    override suspend fun completeResearch(type: ResearchType) {
        val entity = dao.getByType(type.name).first() ?: return
        dao.upsert(entity.copy(level = entity.level + 1, startedAt = null, completesAt = null))
    }

    override suspend fun ensureResearchExists() {
        if (dao.getAll().first().isEmpty()) {
            ResearchType.entries.forEach { dao.upsert(LabResearchEntity(researchType = it.name)) }
        }
    }
}
