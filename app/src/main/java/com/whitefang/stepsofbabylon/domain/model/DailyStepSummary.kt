package com.whitefang.stepsofbabylon.domain.model

data class DailyStepSummary(
    val date: String,
    val sensorSteps: Long = 0,
    val healthConnectSteps: Long = 0,
    val creditedSteps: Long = 0,
    val escrowSteps: Long = 0,
    val escrowSyncCount: Int = 0,
    val activityMinutes: Map<String, Int> = emptyMap(),
    val stepEquivalents: Long = 0,
)
