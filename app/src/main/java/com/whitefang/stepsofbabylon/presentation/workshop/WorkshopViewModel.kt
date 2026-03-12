package com.whitefang.stepsofbabylon.presentation.workshop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitefang.stepsofbabylon.data.local.DailyMissionDao
import com.whitefang.stepsofbabylon.domain.model.DailyMissionType
import com.whitefang.stepsofbabylon.domain.model.UpgradeCategory
import com.whitefang.stepsofbabylon.domain.model.UpgradeType
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository
import com.whitefang.stepsofbabylon.domain.repository.WorkshopRepository
import com.whitefang.stepsofbabylon.domain.usecase.CalculateUpgradeCost
import com.whitefang.stepsofbabylon.domain.usecase.PurchaseUpgrade
import com.whitefang.stepsofbabylon.domain.usecase.QuickInvest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class WorkshopViewModel @Inject constructor(
    private val workshopRepository: WorkshopRepository,
    private val playerRepository: PlayerRepository,
    private val dailyMissionDao: DailyMissionDao,
) : ViewModel() {

    private val calculateCost = CalculateUpgradeCost()
    private val purchaseUpgrade = PurchaseUpgrade(workshopRepository, playerRepository, calculateCost)
    private val quickInvest = QuickInvest(calculateCost)

    private val _selectedCategory = MutableStateFlow(UpgradeCategory.ATTACK)
    private val _processing = MutableStateFlow(false)
    private val _userMessage = MutableStateFlow<String?>(null)
    private val hiddenUpgrades = setOf(UpgradeType.STEP_MULTIPLIER, UpgradeType.RECOVERY_PACKAGES)
    private var allUpgrades: Map<UpgradeType, Int> = emptyMap()

    val uiState: StateFlow<WorkshopUiState> = combine(
        workshopRepository.observeAllUpgrades(),
        playerRepository.observeWallet(),
        _selectedCategory,
        _processing,
        _userMessage,
    ) { upgrades, wallet, category, processing, message ->
        allUpgrades = upgrades
        val filtered = upgrades.filter { (type, _) -> type.category == category && type !in hiddenUpgrades }
        WorkshopUiState(
            upgrades = filtered.map { (type, level) ->
                val maxLevel = type.config.maxLevel
                val isMaxed = maxLevel != null && level >= maxLevel
                val cost = if (isMaxed) 0L else calculateCost(type, level)
                UpgradeDisplayInfo(
                    type = type, level = level, cost = cost, isMaxed = isMaxed,
                    canAfford = !isMaxed && wallet.stepBalance >= cost,
                    description = type.config.description,
                )
            },
            stepBalance = wallet.stepBalance,
            selectedCategory = category,
            isLoading = false,
            isProcessing = processing,
            userMessage = message,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkshopUiState())

    fun selectCategory(category: UpgradeCategory) { _selectedCategory.value = category }

    fun purchase(type: UpgradeType) {
        if (_processing.value) return
        viewModelScope.launch {
            _processing.value = true
            try {
                val level = allUpgrades[type] ?: 0
                val maxLevel = type.config.maxLevel
                if (maxLevel != null && level >= maxLevel) {
                    _userMessage.value = "Already at max level"
                    return@launch
                }
                val wallet = playerRepository.observeWallet().stateIn(viewModelScope).value
                val cost = calculateCost(type, level)
                val success = purchaseUpgrade(type, level, wallet)
                if (success) {
                    try {
                        val today = LocalDate.now().toString()
                        val missions = dailyMissionDao.getByDateOnce(today)
                        val m = missions.find { it.missionType == DailyMissionType.SPEND_5000_WORKSHOP.name && !it.claimed && !it.completed }
                        if (m != null) {
                            val newProgress = m.progress + cost.toInt()
                            dailyMissionDao.updateProgress(m.id, newProgress, newProgress >= m.target)
                        }
                    } catch (_: Exception) { }
                } else {
                    _userMessage.value = "Not enough Steps"
                }
            } finally {
                _processing.value = false
            }
        }
    }

    fun quickInvest() {
        if (_processing.value) return
        viewModelScope.launch {
            _processing.value = true
            try {
                val wallet = playerRepository.observeWallet().stateIn(viewModelScope).value
                val target = quickInvest(allUpgrades, wallet)
                if (target == null) {
                    _userMessage.value = "No affordable upgrades"
                    return@launch
                }
                val level = allUpgrades[target] ?: 0
                purchaseUpgrade(target, level, wallet)
            } finally {
                _processing.value = false
            }
        }
    }

    fun clearMessage() { _userMessage.value = null }
}
