package com.whitefang.stepsofbabylon.domain.model

data class ActiveResearch(
    val type: ResearchType,
    val level: Int,
    val startedAt: Long,
    val completesAt: Long,
)
