package com.whitefang.stepsofbabylon.presentation.labs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitefang.stepsofbabylon.domain.model.ActiveResearch
import com.whitefang.stepsofbabylon.domain.model.ResearchType
import com.whitefang.stepsofbabylon.domain.repository.LabRepository
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository
import com.whitefang.stepsofbabylon.domain.usecase.CalculateResearchCost
import com.whitefang.stepsofbabylon.domain.usecase.CalculateResearchTime
import com.whitefang.stepsofbabylon.domain.usecase.CheckResearchCompletion
import com.whitefang.stepsofbabylon.domain.usecase.RushResearch
import com.whitefang.stepsofbabylon.domain.usecase.StartResearch
import com.whitefang.stepsofbabylon.domain.usecase.UnlockLabSlot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class LabsViewModel @Inject constructor(
    private val labRepository: LabRepository,
    private val playerRepository: PlayerRepository,
) : ViewModel() {

    private val calculateCost = CalculateResearchCost()
    private val calculateTime = CalculateResearchTime()
    private val startResearch = StartResearch(labRepository, playerRepository, calculateCost, calculateTime)
    private val rushResearch = RushResearch(labRepository, playerRepository)
    private val unlockLabSlot = UnlockLabSlot(playerRepository)
    private val checkCompletion = CheckResearchCompletion(labRepository)

    private val tick = MutableStateFlow(System.currentTimeMillis())

    init {
        viewModelScope.launch {
            labRepository.ensureResearchExists()
            checkCompletion()
        }
        viewModelScope.launch {
            while (true) {
                delay(1000)
                tick.value = System.currentTimeMillis()
            }
        }
    }

    val uiState: StateFlow<LabsUiState> = combine(
        labRepository.observeAllResearch(),
        labRepository.observeActiveResearch(),
        playerRepository.observeProfile(),
        tick,
    ) { levels, activeList, profile, now ->
        val activeMap = activeList.associateBy { it.type }
        LabsUiState(
            researchList = ResearchType.entries.map { type ->
                val level = levels[type] ?: 0
                val isMaxed = level >= type.maxLevel
                val active = activeMap[type]
                val cost = if (isMaxed) 0L else calculateCost(type, level)
                val timeHours = if (isMaxed) 0.0 else calculateTime(type, level)
                val remainingMs = active?.let { max(0L, it.completesAt - now) } ?: 0L
                val rushCost = active?.let {
                    RushResearch.calculateRushCost(it.startedAt, it.completesAt, now)
                } ?: 0L
                ResearchDisplayInfo(
                    type = type,
                    level = level,
                    isMaxed = isMaxed,
                    costToStart = cost,
                    canAffordStart = !isMaxed && active == null && profile.stepBalance >= cost,
                    timeToCompleteHours = timeHours,
                    isActive = active != null,
                    remainingMs = remainingMs,
                    rushCostGems = rushCost,
                    canAffordRush = active != null && profile.gems >= rushCost,
                )
            },
            activeSlots = activeList.size,
            totalSlots = profile.labSlotCount,
            stepBalance = profile.stepBalance,
            gems = profile.gems,
            canAffordSlotUnlock = profile.labSlotCount < UnlockLabSlot.MAX_SLOTS && profile.gems >= UnlockLabSlot.SLOT_COST_GEMS,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LabsUiState())

    fun startResearch(type: ResearchType) {
        viewModelScope.launch {
            val profile = playerRepository.observeProfile().stateIn(viewModelScope).value
            startResearch(type, profile.toWallet(), profile.labSlotCount)
        }
    }

    fun rushResearch(type: ResearchType) {
        viewModelScope.launch {
            val profile = playerRepository.observeProfile().stateIn(viewModelScope).value
            val activeList = labRepository.observeActiveResearch().stateIn(viewModelScope).value
            val active = activeList.find { it.type == type } ?: return@launch
            rushResearch(type, active, profile.toWallet())
        }
    }

    fun unlockSlot() {
        viewModelScope.launch {
            val profile = playerRepository.observeProfile().stateIn(viewModelScope).value
            unlockLabSlot(profile.labSlotCount, profile.gems)
        }
    }
}
