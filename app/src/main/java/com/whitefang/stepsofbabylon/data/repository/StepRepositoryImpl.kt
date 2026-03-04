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

    override suspend fun updateGoogleFitSteps(date: String, googleFitSteps: Long) {
        val existing = dao.getByDateOnce(date) ?: DailyStepRecordEntity(date = date)
        dao.upsert(existing.copy(googleFitSteps = googleFitSteps))
    }

    override suspend fun updateActivityMinutes(date: String, activityMinutes: Map<String, Int>, stepEquivalents: Long) {
        val existing = dao.getByDateOnce(date) ?: DailyStepRecordEntity(date = date)
        dao.upsert(existing.copy(activityMinutes = activityMinutes, stepEquivalents = stepEquivalents))
    }

    private fun DailyStepRecordEntity.toDomain() = DailyStepSummary(
        date = date,
        sensorSteps = sensorSteps,
        googleFitSteps = googleFitSteps,
        creditedSteps = creditedSteps,
        activityMinutes = activityMinutes,
        stepEquivalents = stepEquivalents,
    )
}
