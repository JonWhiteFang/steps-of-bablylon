package com.whitefang.stepsofbabylon.presentation.battle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitefang.stepsofbabylon.domain.model.ResolvedStats
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository
import com.whitefang.stepsofbabylon.domain.repository.WorkshopRepository
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
    var resolvedStats: ResolvedStats = ResolvedStats(); private set

    init {
        viewModelScope.launch {
            val workshopLevels = workshopRepository.observeAllUpgrades().first()
            resolvedStats = resolveStats(workshopLevels)
            _uiState.update { it.copy(maxHp = resolvedStats.maxHealth, currentHp = resolvedStats.maxHealth, isLoading = false) }
        }
    }

    fun startPollingEngine(engine: GameEngine) {
        engine.setStats(resolvedStats)
        viewModelScope.launch {
            while (true) {
                delay(200)
                val zig = engine.ziggurat ?: continue
                val spawner = engine.waveSpawner
                _uiState.update {
                    it.copy(
                        currentWave = spawner?.currentWave ?: 1,
                        currentHp = zig.currentHp,
                        maxHp = zig.maxHp,
                        cash = engine.cash,
                        enemyCount = spawner?.enemiesAlive ?: 0,
                        wavePhase = spawner?.phase?.name ?: "",
                    )
                }
                if (engine.roundOver) {
                    _events.emit(BattleEvent.RoundEnded)
                    break
                }
            }
        }
    }

    fun setSpeed(multiplier: Float) { _uiState.update { it.copy(speedMultiplier = multiplier) } }
    fun togglePause() { _uiState.update { it.copy(isPaused = !it.isPaused) } }
    fun onRoundEnd() { viewModelScope.launch { _events.emit(BattleEvent.RoundEnded) } }
}
