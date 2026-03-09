package com.whitefang.stepsofbabylon.presentation.missions

import com.whitefang.stepsofbabylon.domain.model.Milestone

data class MissionDisplayInfo(
    val id: Int,
    val description: String,
    val target: Int,
    val progress: Int,
    val rewardGems: Int,
    val rewardPowerStones: Int,
    val completed: Boolean,
    val claimed: Boolean,
)

data class MilestoneDisplayInfo(
    val milestone: Milestone,
    val isAchieved: Boolean,
    val isClaimed: Boolean,
    val totalStepsEarned: Long,
)

data class MissionsUiState(
    val missions: List<MissionDisplayInfo> = emptyList(),
    val milestones: List<MilestoneDisplayInfo> = emptyList(),
    val timeUntilMidnightMs: Long = 0,
    val isLoading: Boolean = true,
)
