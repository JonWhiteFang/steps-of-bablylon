package com.whitefang.stepsofbabylon.presentation.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitefang.stepsofbabylon.domain.model.OwnedCard
import com.whitefang.stepsofbabylon.domain.model.AdPlacement
import com.whitefang.stepsofbabylon.domain.model.AdResult
import com.whitefang.stepsofbabylon.domain.usecase.CardResult
import com.whitefang.stepsofbabylon.domain.usecase.ManageCardLoadout
import com.whitefang.stepsofbabylon.domain.usecase.OpenCardPack
import com.whitefang.stepsofbabylon.domain.usecase.PackTier
import com.whitefang.stepsofbabylon.domain.usecase.UpgradeCard
import com.whitefang.stepsofbabylon.domain.repository.CardRepository
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository
import com.whitefang.stepsofbabylon.domain.repository.RewardAdManager
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
class CardsViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val playerRepository: PlayerRepository,
    private val rewardAdManager: RewardAdManager,
) : ViewModel() {

    private val openCardPack = OpenCardPack(cardRepository, playerRepository)
    private val upgradeCard = UpgradeCard(cardRepository, playerRepository)
    private val manageLoadout = ManageCardLoadout(cardRepository)

    private val _lastPackResult = MutableStateFlow<List<CardResult>?>(null)
    private var allCards: List<OwnedCard> = emptyList()

    val uiState: StateFlow<CardsUiState> = combine(
        cardRepository.observeAllCards(),
        playerRepository.observeProfile(),
        _lastPackResult,
    ) { cards, profile, packResult ->
        allCards = cards
        val equipped = cards.count { it.isEquipped }
        CardsUiState(
            ownedCards = cards.map { card ->
                val isMax = card.level >= card.type.maxLevel
                val dustCost = if (isMax) 0L else card.level * card.type.rarity.upgradeDustPerLevel
                CardDisplayInfo(
                    id = card.id, type = card.type, level = card.level,
                    isEquipped = card.isEquipped, isMaxLevel = isMax,
                    upgradeDustCost = dustCost,
                    canAffordUpgrade = !isMax && profile.cardDust >= dustCost,
                    effectDescription = card.type.effectLv1, // simplified — shows lv1 description
                )
            },
            equippedCount = equipped,
            gems = profile.gems,
            cardDust = profile.cardDust,
            packOptions = PackTier.entries.map { PackOption(it, profile.gems >= it.gemCost) },
            lastPackResult = packResult,
            freePackAvailable = !profile.adRemoved && profile.freeCardPackAdUsedToday != LocalDate.now().toString(),
            adRemoved = profile.adRemoved,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CardsUiState())

    fun openPack(packTier: PackTier) {
        viewModelScope.launch {
            val profile = playerRepository.observeProfile().stateIn(viewModelScope).value
            val result = openCardPack(packTier, profile.gems, allCards)
            if (result is OpenCardPack.Result.Opened) _lastPackResult.value = result.cards
        }
    }

    fun upgradeCard(cardId: Int) {
        viewModelScope.launch {
            val card = allCards.find { it.id == cardId } ?: return@launch
            val profile = playerRepository.observeProfile().stateIn(viewModelScope).value
            upgradeCard(card, profile.cardDust)
        }
    }

    fun equipCard(cardId: Int) {
        viewModelScope.launch {
            val equipped = allCards.count { it.isEquipped }
            manageLoadout.equip(cardId, equipped)
        }
    }

    fun unequipCard(cardId: Int) {
        viewModelScope.launch { manageLoadout.unequip(cardId) }
    }

    fun dismissPackResult() { _lastPackResult.value = null }

    fun watchFreePackAd() {
        viewModelScope.launch {
            val result = rewardAdManager.showRewardAd(AdPlacement.DAILY_FREE_CARD_PACK)
            if (result is AdResult.Rewarded) {
                val profile = playerRepository.observeProfile().stateIn(viewModelScope).value
                val packResult = openCardPack(PackTier.COMMON, profile.gems, allCards, isFree = true)
                if (packResult is OpenCardPack.Result.Opened) _lastPackResult.value = packResult.cards
                playerRepository.updateFreeCardPackAdUsed(LocalDate.now().toString())
            }
        }
    }
}
