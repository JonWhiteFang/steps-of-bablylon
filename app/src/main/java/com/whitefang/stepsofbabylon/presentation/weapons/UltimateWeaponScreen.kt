package com.whitefang.stepsofbabylon.presentation.weapons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun UltimateWeaponScreen(viewModel: UltimateWeaponViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        Text("Power Stones: ${state.powerStones}", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
        Text("Equipped: ${state.equippedCount}/3", style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray)

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.weapons, key = { it.type.name }) { info ->
                UWCard(info = info, canEquipMore = state.equippedCount < 3,
                    onUnlock = { viewModel.unlock(info.type) },
                    onUpgrade = { viewModel.upgrade(info.type) },
                    onToggleEquip = { viewModel.toggleEquip(info.type) })
            }
        }
    }
}

@Composable
private fun UWCard(
    info: UWDisplayInfo, canEquipMore: Boolean,
    onUnlock: () -> Unit, onUpgrade: () -> Unit, onToggleEquip: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (info.owned) Color(0xFF2A2A3E) else Color(0xFF1A1A2E)),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(info.type.name.replace('_', ' '), fontWeight = FontWeight.Bold,
                        color = if (info.owned) Color.White else Color.Gray)
                    Text(info.type.description, style = MaterialTheme.typography.bodySmall,
                        color = if (info.owned) Color.White.copy(alpha = 0.7f) else Color.Gray.copy(alpha = 0.5f))
                    if (info.owned) {
                        Text("Level ${info.level} · CD: ${info.type.cooldownAtLevel(info.level).toInt()}s",
                            style = MaterialTheme.typography.labelSmall, color = Color(0xFFD4A843))
                    }
                }
                if (info.isEquipped) {
                    Text("✓", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                if (!info.owned) {
                    Button(onClick = onUnlock, enabled = info.canAffordUnlock,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD))) {
                        Text("Unlock (${info.type.unlockCost} PS)")
                    }
                } else {
                    OutlinedButton(onClick = onToggleEquip, enabled = info.isEquipped || canEquipMore) {
                        Text(if (info.isEquipped) "Unequip" else "Equip")
                    }
                    if (!info.isMaxLevel) {
                        Button(onClick = onUpgrade, enabled = info.canAffordUpgrade,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD))) {
                            Text("Upgrade (${info.upgradeCost} PS)")
                        }
                    } else {
                        Text("MAX", color = Color(0xFFD4A843), fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterVertically))
                    }
                }
            }
        }
    }
}
