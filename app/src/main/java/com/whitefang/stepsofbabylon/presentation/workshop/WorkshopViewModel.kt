package com.whitefang.stepsofbabylon.presentation.workshop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkshopViewModel @Inject constructor(
    private val workshopRepository: WorkshopRepository,
    private val playerRepository: PlayerRepository,
) : ViewModel() {

    private val calculateCost = CalculateUpgradeCost()
    private val purchaseUpgrade = PurchaseUpgrade(workshopRepository, playerRepository, calculateCost)
    private val quickInvest = QuickInvest(calculateCost)

    private val _selectedCategory = MutableStateFlow(UpgradeCategory.ATTACK)
    private var allUpgrades: Map<UpgradeType, Int> = emptyMap()

    val uiState: StateFlow<WorkshopUiState> = combine(
        workshopRepository.observeAllUpgrades(),
        playerRepository.observeWallet(),
        _selectedCategory,
    ) { upgrades, wallet, category ->
        allUpgrades = upgrades
        val filtered = upgrades.filter { (type, _) -> type.category == category }
        WorkshopUiState(
            upgrades = filtered.map { (type, level) ->
                val maxLevel = type.config.maxLevel
                val isMaxed = maxLevel != null && level >= maxLevel
                val cost = if (isMaxed) 0L else calculateCost(type, level)
                UpgradeDisplayInfo(
                    type = type,
                    level = level,
                    cost = cost,
                    isMaxed = isMaxed,
                    canAfford = !isMaxed && wallet.stepBalance >= cost,
                    description = type.config.description,
                )
            },
            stepBalance = wallet.stepBalance,
            selectedCategory = category,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkshopUiState())

    fun selectCategory(category: UpgradeCategory) {
        _selectedCategory.value = category
    }

    fun purchase(type: UpgradeType) {
        viewModelScope.launch {
            val level = allUpgrades[type] ?: 0
            val wallet = playerRepository.observeWallet().stateIn(viewModelScope).value
            purchaseUpgrade(type, level, wallet)
        }
    }

    fun quickInvest() {
        viewModelScope.launch {
            val wallet = playerRepository.observeWallet().stateIn(viewModelScope).value
            val target = quickInvest(allUpgrades, wallet) ?: return@launch
            val level = allUpgrades[target] ?: 0
            purchaseUpgrade(target, level, wallet)
        }
    }
}
