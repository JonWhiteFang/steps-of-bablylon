package com.whitefang.stepsofbabylon.presentation.economy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitefang.stepsofbabylon.data.local.DailyLoginDao
import com.whitefang.stepsofbabylon.data.local.DailyStepDao
import com.whitefang.stepsofbabylon.data.local.WeeklyChallengeDao
import com.whitefang.stepsofbabylon.data.local.WeeklyChallengeEntity
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class CurrencyDashboardViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val weeklyChallengeDao: WeeklyChallengeDao,
    private val dailyLoginDao: DailyLoginDao,
    private val dailyStepDao: DailyStepDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EconomyUiState())
    val uiState: StateFlow<EconomyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { loadState() }
    }

    private suspend fun loadState() {
        val today = LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sunday = monday.plusDays(6)
        val weekStart = monday.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val weekEnd = sunday.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val profile = playerRepository.observeProfile().first()
        val weeklySteps = dailyStepDao.sumCreditedSteps(weekStart, weekEnd)
        val weekly = weeklyChallengeDao.getByWeek(weekStart) ?: WeeklyChallengeEntity(weekStartDate = weekStart)
        val login = dailyLoginDao.getByDate(todayStr)

        _uiState.update {
            EconomyUiState(
                gems = profile.gems,
                powerStones = profile.powerStones,
                weeklySteps = weeklySteps,
                weeklyClaimedTier = weekly.claimedTier,
                currentStreak = profile.currentStreak,
                todayPsClaimed = login?.powerStoneClaimed ?: false,
                todayGemsClaimed = login?.gemsClaimed ?: false,
                isLoading = false,
            )
        }
    }
}
