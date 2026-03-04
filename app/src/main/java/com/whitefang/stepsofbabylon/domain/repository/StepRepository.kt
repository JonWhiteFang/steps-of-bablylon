package com.whitefang.stepsofbabylon.domain.repository

import com.whitefang.stepsofbabylon.domain.model.DailyStepSummary
import kotlinx.coroutines.flow.Flow

interface StepRepository {
    fun observeTodayRecord(date: String): Flow<DailyStepSummary?>
    fun observeHistory(startDate: String, endDate: String): Flow<List<DailyStepSummary>>
    suspend fun updateDailySteps(date: String, sensorSteps: Long, creditedSteps: Long)
    suspend fun getDailyRecord(date: String): DailyStepSummary?
    suspend fun updateHealthConnectSteps(date: String, healthConnectSteps: Long)
    suspend fun updateActivityMinutes(date: String, activityMinutes: Map<String, Int>, stepEquivalents: Long)
    suspend fun updateEscrow(date: String, escrowSteps: Long, syncCount: Int)
    suspend fun releaseEscrow(date: String)
    suspend fun discardEscrow(date: String)
}
