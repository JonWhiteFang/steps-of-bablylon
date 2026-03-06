package com.whitefang.stepsofbabylon.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitefang.stepsofbabylon.domain.model.Biome
import com.whitefang.stepsofbabylon.domain.repository.LabRepository
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository
import com.whitefang.stepsofbabylon.domain.repository.StepRepository
import com.whitefang.stepsofbabylon.domain.repository.WalkingEncounterRepository
import com.whitefang.stepsofbabylon.domain.repository.WorkshopRepository
import com.whitefang.stepsofbabylon.domain.usecase.CheckResearchCompletion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val stepRepository: StepRepository,
    private val workshopRepository: WorkshopRepository,
    private val labRepository: LabRepository,
    private val walkingEncounterRepository: WalkingEncounterRepository,
) : ViewModel() {

    init {
        viewModelScope.launch {
            playerRepository.ensureProfileExists()
            workshopRepository.ensureUpgradesExist()
            labRepository.ensureResearchExists()
            CheckResearchCompletion(labRepository)()
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        playerRepository.observeProfile(),
        stepRepository.observeTodayRecord(LocalDate.now().toString()),
        walkingEncounterRepository.countUnclaimed(),
    ) { profile, stepSummary, unclaimedCount ->
        HomeUiState(
            todaySteps = stepSummary?.creditedSteps ?: 0,
            stepBalance = profile.stepBalance,
            gems = profile.gems,
            powerStones = profile.powerStones,
            currentTier = profile.currentTier,
            highestUnlockedTier = profile.highestUnlockedTier,
            currentBiome = Biome.forTier(profile.currentTier),
            bestWave = profile.bestWavePerTier[profile.currentTier] ?: 0,
            bestWavePerTier = profile.bestWavePerTier,
            unclaimedDropCount = unclaimedCount,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun selectTier(tier: Int) {
        viewModelScope.launch { playerRepository.updateTier(tier) }
    }
}
