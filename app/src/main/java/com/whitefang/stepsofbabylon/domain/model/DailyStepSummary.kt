package com.whitefang.stepsofbabylon.domain.model

data class DailyStepSummary(
    val date: String,
    val sensorSteps: Long = 0,
    val googleFitSteps: Long = 0,
    val creditedSteps: Long = 0,
    val activityMinutes: Map<String, Int> = emptyMap(),
    val stepEquivalents: Long = 0,
)
