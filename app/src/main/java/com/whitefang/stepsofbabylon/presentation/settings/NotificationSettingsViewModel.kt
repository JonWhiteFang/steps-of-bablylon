package com.whitefang.stepsofbabylon.presentation.settings

import androidx.lifecycle.ViewModel
import com.whitefang.stepsofbabylon.data.NotificationPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class NotificationSettingsState(
    val persistentSteps: Boolean = true,
    val supplyDrops: Boolean = true,
    val smartReminders: Boolean = true,
    val milestoneAlerts: Boolean = true,
)

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val prefs: NotificationPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationSettingsState(
        persistentSteps = prefs.isPersistentEnabled(),
        supplyDrops = prefs.isSupplyDropsEnabled(),
        smartReminders = prefs.isSmartRemindersEnabled(),
        milestoneAlerts = prefs.isMilestoneAlertsEnabled(),
    ))
    val state: StateFlow<NotificationSettingsState> = _state.asStateFlow()

    fun setPersistent(enabled: Boolean) { prefs.setPersistentEnabled(enabled); _state.update { it.copy(persistentSteps = enabled) } }
    fun setSupplyDrops(enabled: Boolean) { prefs.setSupplyDropsEnabled(enabled); _state.update { it.copy(supplyDrops = enabled) } }
    fun setSmartReminders(enabled: Boolean) { prefs.setSmartRemindersEnabled(enabled); _state.update { it.copy(smartReminders = enabled) } }
    fun setMilestoneAlerts(enabled: Boolean) { prefs.setMilestoneAlertsEnabled(enabled); _state.update { it.copy(milestoneAlerts = enabled) } }
}
