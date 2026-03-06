package com.whitefang.stepsofbabylon.domain.model

data class DropGeneratorState(
    val lastCheckSteps: Long = 0,
    val milestoneTriggered: Boolean = false,
)
