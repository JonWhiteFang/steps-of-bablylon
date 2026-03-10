package com.whitefang.stepsofbabylon.fakes

import com.whitefang.stepsofbabylon.domain.model.DailyStepSummary
import com.whitefang.stepsofbabylon.domain.repository.StepRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeStepRepository : StepRepository {

    val records = MutableStateFlow<Map<String, DailyStepSummary>>(emptyMap())

    override fun observeTodayRecord(date: String): Flow<DailyStepSummary?> =
        records.map { it[date] }

    override fun observeHistory(startDate: String, endDate: String): Flow<List<DailyStepSummary>> =
        records.map { map -> map.values.filter { it.date in startDate..endDate }.sortedBy { it.date } }

    override suspend fun getDailyRecord(date: String): DailyStepSummary? = records.value[date]

    override suspend fun updateDailySteps(date: String, sensorSteps: Long, creditedSteps: Long) {
        val existing = records.value[date] ?: DailyStepSummary(date)
        records.value = records.value + (date to existing.copy(sensorSteps = sensorSteps, creditedSteps = creditedSteps))
    }

    override suspend fun updateHealthConnectSteps(date: String, healthConnectSteps: Long) {
        val existing = records.value[date] ?: DailyStepSummary(date)
        records.value = records.value + (date to existing.copy(healthConnectSteps = healthConnectSteps))
    }

    override suspend fun updateActivityMinutes(date: String, activityMinutes: Map<String, Int>, stepEquivalents: Long) {
        val existing = records.value[date] ?: DailyStepSummary(date)
        records.value = records.value + (date to existing.copy(activityMinutes = activityMinutes, stepEquivalents = stepEquivalents))
    }

    override suspend fun updateEscrow(date: String, escrowSteps: Long, syncCount: Int) {
        val existing = records.value[date] ?: DailyStepSummary(date)
        records.value = records.value + (date to existing.copy(escrowSteps = escrowSteps, escrowSyncCount = syncCount))
    }

    override suspend fun releaseEscrow(date: String) {
        val existing = records.value[date] ?: return
        records.value = records.value + (date to existing.copy(
            creditedSteps = existing.creditedSteps + existing.escrowSteps,
            escrowSteps = 0, escrowSyncCount = 0
        ))
    }

    override suspend fun discardEscrow(date: String) {
        val existing = records.value[date] ?: return
        records.value = records.value + (date to existing.copy(escrowSteps = 0, escrowSyncCount = 0))
    }
}
