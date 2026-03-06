package com.whitefang.stepsofbabylon.presentation.battle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitefang.stepsofbabylon.domain.model.ResolvedStats
import com.whitefang.stepsofbabylon.domain.model.UpgradeType
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository
import com.whitefang.stepsofbabylon.domain.repository.WorkshopRepository
import com.whitefang.stepsofbabylon.domain.usecase.CalculateUpgradeCost
import com.whitefang.stepsofbabylon.domain.usecase.ResolveStats
import com.whitefang.stepsofbabylon.presentation.battle.engine.GameEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.min
import kotlin.random.Random

sealed interface BattleEvent {
    data object RoundEnded : BattleEvent
}

@HiltViewModel
class BattleViewModel @Inject constructor(
    private val workshopRepository: WorkshopRepository,
    private val playerRepository: PlayerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BattleUiState())
    val uiState: StateFlow<BattleUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<BattleEvent>()
    val events = _events.asSharedFlow()

    private val resolveStats = ResolveStats()
    private val calculateCost = CalculateUpgradeCost()

    var resolvedStats: ResolvedStats = ResolvedStats(); private set
    private var workshopLevels: Map<UpgradeType, Int> = emptyMap()
    private val inRoundLevels = mutableMapOf<UpgradeType, Int>()
    private var engine: GameEngine? = null
    var tier: Int = 1; private set

    init {
        viewModelScope.launch {
            workshopLevels = workshopRepository.observeAllUpgrades().first()
            tier = playerRepository.observeTier().first()
            resolvedStats = resolveStats(workshopLevels)
            _uiState.update { it.copy(maxHp = resolvedStats.maxHealth, currentHp = resolvedStats.maxHealth, isLoading = false) }
        }
    }

    fun startPollingEngine(engine: GameEngine) {
        this.engine = engine
        engine.setStats(resolvedStats)
        viewModelScope.launch {
            while (true) {
                delay(200)
                val zig = engine.ziggurat ?: continue
                val spawner = engine.waveSpawner
                _uiState.update {
                    it.copy(
                        currentWave = spawner?.currentWave ?: 1,
                        currentHp = zig.currentHp, maxHp = zig.maxHp,
                        cash = engine.cash,
                        enemyCount = spawner?.enemiesAlive ?: 0,
                        wavePhase = spawner?.phase?.name ?: "",
                    )
                }
                if (engine.roundOver) { _events.emit(BattleEvent.RoundEnded); break }
            }
        }
    }

    fun purchaseInRoundUpgrade(type: UpgradeType) {
        val eng = engine ?: return
        val currentLevel = inRoundLevels[type] ?: 0
        val maxLevel = type.config.maxLevel
        if (maxLevel != null && currentLevel >= maxLevel) return

        val cost = calculateCost(type, currentLevel)

        // Free upgrade chance
        val freeLevel = workshopLevels[UpgradeType.FREE_UPGRADES] ?: 0
        val freeChance = min(freeLevel * 0.01, 0.25)
        val isFree = freeChance > 0 && Random.nextDouble() < freeChance

        if (!isFree && !eng.spendCash(cost)) return

        inRoundLevels[type] = currentLevel + 1
        resolvedStats = resolveStats(workshopLevels, inRoundLevels)
        eng.updateZigguratStats(resolvedStats)

        _uiState.update { it.copy(inRoundLevels = inRoundLevels.toMap(), lastPurchaseFree = isFree) }
    }

    fun toggleUpgradeMenu() { _uiState.update { it.copy(showUpgradeMenu = !it.showUpgradeMenu) } }
    fun setSpeed(multiplier: Float) { _uiState.update { it.copy(speedMultiplier = multiplier) } }
    fun togglePause() { _uiState.update { it.copy(isPaused = !it.isPaused) } }
    fun onRoundEnd() { viewModelScope.launch { _events.emit(BattleEvent.RoundEnded) } }
}
