package com.whitefang.stepsofbabylon.data.repository

import com.whitefang.stepsofbabylon.data.local.DailyStepDao
import com.whitefang.stepsofbabylon.data.local.DailyStepRecordEntity
import com.whitefang.stepsofbabylon.domain.model.DailyStepSummary
import com.whitefang.stepsofbabylon.domain.repository.StepRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StepRepositoryImpl @Inject constructor(
    private val dao: DailyStepDao,
) : StepRepository {

    override fun observeTodayRecord(date: String): Flow<DailyStepSummary?> =
        dao.getByDate(date).map { it?.toDomain() }

    override fun observeHistory(startDate: String, endDate: String): Flow<List<DailyStepSummary>> =
        dao.getRange(startDate, endDate).map { list -> list.map { it.toDomain() } }

    override suspend fun updateDailySteps(date: String, sensorSteps: Long, creditedSteps: Long) {
        val existing = dao.getByDateOnce(date) ?: DailyStepRecordEntity(date = date)
        dao.upsert(existing.copy(sensorSteps = sensorSteps, creditedSteps = creditedSteps))
    }

    override suspend fun getDailyRecord(date: String): DailyStepSummary? =
        dao.getByDateOnce(date)?.toDomain()

    override suspend fun updateHealthConnectSteps(date: String, healthConnectSteps: Long) {
        val existing = dao.getByDateOnce(date) ?: DailyStepRecordEntity(date = date)
        dao.upsert(existing.copy(healthConnectSteps = healthConnectSteps))
    }

    override suspend fun updateActivityMinutes(date: String, activityMinutes: Map<String, Int>, stepEquivalents: Long) {
        val existing = dao.getByDateOnce(date) ?: DailyStepRecordEntity(date = date)
        dao.upsert(existing.copy(activityMinutes = activityMinutes, stepEquivalents = stepEquivalents))
    }

    override suspend fun updateEscrow(date: String, escrowSteps: Long, syncCount: Int) {
        val existing = dao.getByDateOnce(date) ?: DailyStepRecordEntity(date = date)
        dao.upsert(existing.copy(escrowSteps = escrowSteps, escrowSyncCount = syncCount))
    }

    override suspend fun releaseEscrow(date: String) = dao.clearEscrow(date)

    override suspend fun discardEscrow(date: String) = dao.clearEscrow(date)

    private fun DailyStepRecordEntity.toDomain() = DailyStepSummary(
        date = date,
        sensorSteps = sensorSteps,
        healthConnectSteps = healthConnectSteps,
        creditedSteps = creditedSteps,
        escrowSteps = escrowSteps,
        escrowSyncCount = escrowSyncCount,
        activityMinutes = activityMinutes,
        stepEquivalents = stepEquivalents,
    )
}
