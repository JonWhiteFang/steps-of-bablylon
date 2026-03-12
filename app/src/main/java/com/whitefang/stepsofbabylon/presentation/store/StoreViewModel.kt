package com.whitefang.stepsofbabylon.presentation.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitefang.stepsofbabylon.domain.model.BillingProduct
import com.whitefang.stepsofbabylon.domain.model.PurchaseResult
import com.whitefang.stepsofbabylon.domain.repository.BillingManager
import com.whitefang.stepsofbabylon.domain.repository.CosmeticRepository
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val billingManager: BillingManager,
    private val cosmeticRepository: CosmeticRepository,
) : ViewModel() {

    init { viewModelScope.launch { cosmeticRepository.ensureSeedData() } }

    val uiState: StateFlow<StoreUiState> = combine(
        playerRepository.observeProfile(),
        cosmeticRepository.observeAll(),
    ) { profile, cosmetics ->
        StoreUiState(
            gems = profile.gems,
            adRemoved = profile.adRemoved,
            seasonPassActive = profile.seasonPassActive && profile.seasonPassExpiry > System.currentTimeMillis(),
            seasonPassExpiry = profile.seasonPassExpiry,
            cosmetics = cosmetics.map {
                CosmeticDisplayInfo(it.cosmeticId, it.category.name, it.name, it.description, it.priceGems, it.isOwned, it.isEquipped)
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StoreUiState())

    fun purchaseGemPack(product: BillingProduct) {
        viewModelScope.launch { billingManager.purchase(product) }
    }

    fun purchaseAdRemoval() {
        viewModelScope.launch { billingManager.purchase(BillingProduct.AD_REMOVAL) }
    }

    fun purchaseSeasonPass() {
        viewModelScope.launch { billingManager.purchase(BillingProduct.SEASON_PASS) }
    }

    fun purchaseCosmetic(cosmeticId: String) {
        viewModelScope.launch {
            val profile = playerRepository.observeProfile().stateIn(viewModelScope).value
            val cosmetic = uiState.value.cosmetics.find { it.cosmeticId == cosmeticId } ?: return@launch
            if (profile.gems >= cosmetic.priceGems) {
                playerRepository.spendGems(cosmetic.priceGems)
                cosmeticRepository.purchase(cosmeticId)
            }
        }
    }

    fun equipCosmetic(cosmeticId: String) {
        viewModelScope.launch { cosmeticRepository.equip(cosmeticId) }
    }

    fun unequipCosmetic(cosmeticId: String) {
        viewModelScope.launch { cosmeticRepository.unequip(cosmeticId) }
    }
}
