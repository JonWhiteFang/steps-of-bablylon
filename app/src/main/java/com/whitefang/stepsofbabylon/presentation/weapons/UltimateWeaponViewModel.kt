package com.whitefang.stepsofbabylon.presentation.weapons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitefang.stepsofbabylon.domain.model.OwnedWeapon
import com.whitefang.stepsofbabylon.domain.model.UltimateWeaponType
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository
import com.whitefang.stepsofbabylon.domain.repository.UltimateWeaponRepository
import com.whitefang.stepsofbabylon.domain.usecase.UnlockUltimateWeapon
import com.whitefang.stepsofbabylon.domain.usecase.UpgradeUltimateWeapon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UWDisplayInfo(
    val type: UltimateWeaponType,
    val owned: Boolean,
    val level: Int,
    val isEquipped: Boolean,
    val canAffordUnlock: Boolean,
    val upgradeCost: Int,
    val canAffordUpgrade: Boolean,
    val isMaxLevel: Boolean,
)

data class UltimateWeaponUiState(
    val weapons: List<UWDisplayInfo> = emptyList(),
    val powerStones: Long = 0,
    val equippedCount: Int = 0,
)

@HiltViewModel
class UltimateWeaponViewModel @Inject constructor(
    private val uwRepository: UltimateWeaponRepository,
    private val playerRepository: PlayerRepository,
) : ViewModel() {

    private val unlockUW = UnlockUltimateWeapon(uwRepository, playerRepository)
    private val upgradeUW = UpgradeUltimateWeapon(uwRepository, playerRepository)
    private var ownedList: List<OwnedWeapon> = emptyList()

    val uiState: StateFlow<UltimateWeaponUiState> = combine(
        uwRepository.observeUnlockedWeapons(),
        playerRepository.observeWallet(),
    ) { owned, wallet ->
        ownedList = owned
        val ownedMap = owned.associateBy { it.type }
        UltimateWeaponUiState(
            weapons = UltimateWeaponType.entries.map { type ->
                val ow = ownedMap[type]
                val level = ow?.level ?: 0
                val upgCost = if (ow != null) type.upgradeCost(level) else 0
                UWDisplayInfo(
                    type = type, owned = ow != null, level = level,
                    isEquipped = ow?.isEquipped == true,
                    canAffordUnlock = ow == null && wallet.powerStones >= type.unlockCost,
                    upgradeCost = upgCost,
                    canAffordUpgrade = ow != null && level < UltimateWeaponType.MAX_LEVEL && wallet.powerStones >= upgCost,
                    isMaxLevel = level >= UltimateWeaponType.MAX_LEVEL,
                )
            },
            powerStones = wallet.powerStones,
            equippedCount = owned.count { it.isEquipped },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UltimateWeaponUiState())

    fun unlock(type: UltimateWeaponType) {
        viewModelScope.launch { unlockUW(type, uiState.value.powerStones, ownedList) }
    }

    fun upgrade(type: UltimateWeaponType) {
        val info = uiState.value.weapons.find { it.type == type } ?: return
        viewModelScope.launch { upgradeUW(type, info.level, uiState.value.powerStones) }
    }

    fun toggleEquip(type: UltimateWeaponType) {
        val info = uiState.value.weapons.find { it.type == type } ?: return
        if (!info.owned) return
        viewModelScope.launch {
            if (info.isEquipped) uwRepository.unequipWeapon(type)
            else if (uiState.value.equippedCount < 3) uwRepository.equipWeapon(type)
        }
    }
}
