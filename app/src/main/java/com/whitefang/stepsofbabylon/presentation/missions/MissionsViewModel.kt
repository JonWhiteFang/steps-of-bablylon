package com.whitefang.stepsofbabylon.presentation.missions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitefang.stepsofbabylon.data.local.DailyMissionDao
import com.whitefang.stepsofbabylon.data.local.DailyStepDao
import com.whitefang.stepsofbabylon.data.local.MilestoneDao
import com.whitefang.stepsofbabylon.domain.model.DailyMissionType
import com.whitefang.stepsofbabylon.domain.model.Milestone
import com.whitefang.stepsofbabylon.domain.model.MissionCategory
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository
import com.whitefang.stepsofbabylon.domain.usecase.ClaimMilestone
import com.whitefang.stepsofbabylon.domain.usecase.GenerateDailyMissions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.Duration
import javax.inject.Inject

@HiltViewModel
class MissionsViewModel @Inject constructor(
    private val dailyMissionDao: DailyMissionDao,
    private val milestoneDao: MilestoneDao,
    private val dailyStepDao: DailyStepDao,
    private val playerRepository: PlayerRepository,
) : ViewModel() {

    private val generateMissions = GenerateDailyMissions(dailyMissionDao)
    private val claimMilestone = ClaimMilestone(milestoneDao, playerRepository)
    private val today = LocalDate.now().toString()
    private val tick = MutableStateFlow(System.currentTimeMillis())

    init {
        viewModelScope.launch { generateMissions(today) }
        viewModelScope.launch {
            // Auto-update walking mission progress from step data
            updateWalkingMissionProgress()
        }
        viewModelScope.launch {
            while (true) {
                delay(1000)
                tick.value = System.currentTimeMillis()
            }
        }
    }

    val uiState: StateFlow<MissionsUiState> = combine(
        dailyMissionDao.getByDate(today),
        milestoneDao.getAll(),
        playerRepository.observeProfile(),
        tick,
    ) { missions, claimedMilestones, profile, now ->
        val claimedIds = claimedMilestones.filter { it.claimed }.map { it.milestoneId }.toSet()
        val midnight = Duration.between(LocalTime.now(), LocalTime.MIDNIGHT.minusNanos(1)).toMillis()
            .let { if (it < 0) it + 86_400_000 else it }

        MissionsUiState(
            missions = missions.map { m ->
                MissionDisplayInfo(m.id, m.missionType.let { type ->
                    DailyMissionType.entries.find { it.name == type }?.description ?: type
                }, m.target, m.progress, m.rewardGems, m.rewardPowerStones, m.completed, m.claimed)
            },
            milestones = Milestone.entries.map { ms ->
                MilestoneDisplayInfo(ms, profile.totalStepsEarned >= ms.requiredSteps, ms.name in claimedIds, profile.totalStepsEarned)
            },
            timeUntilMidnightMs = midnight,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MissionsUiState())

    fun claimMission(id: Int) {
        viewModelScope.launch {
            val missions = dailyMissionDao.getByDateOnce(today)
            val m = missions.find { it.id == id && it.completed && !it.claimed } ?: return@launch
            if (m.rewardGems > 0) playerRepository.addGems(m.rewardGems.toLong())
            if (m.rewardPowerStones > 0) playerRepository.addPowerStones(m.rewardPowerStones.toLong())
            dailyMissionDao.markClaimed(id)
        }
    }

    fun claimMilestone(milestone: Milestone) {
        viewModelScope.launch { claimMilestone.invoke(milestone) }
    }

    private suspend fun updateWalkingMissionProgress() {
        val missions = dailyMissionDao.getByDateOnce(today)
        val todaySteps = dailyStepDao.sumCreditedSteps(today, today)
        for (m in missions) {
            if (m.claimed || m.completed) continue
            val type = DailyMissionType.entries.find { it.name == m.missionType } ?: continue
            if (type.category != MissionCategory.WALKING) continue
            val progress = todaySteps.toInt().coerceAtMost(m.target)
            dailyMissionDao.updateProgress(m.id, progress, progress >= m.target)
        }
    }
}
