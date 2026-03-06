package com.whitefang.stepsofbabylon.presentation.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.whitefang.stepsofbabylon.domain.model.CardRarity

@Composable
fun CardsScreen(viewModel: CardsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("💎 ${state.gems}", style = MaterialTheme.typography.titleMedium)
            Text("✨ ${state.cardDust} Dust", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(8.dp))
        Text("Equipped: ${state.equippedCount}/3", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))

        // Pack buttons
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.packOptions.forEach { pack ->
                Button(
                    onClick = { viewModel.openPack(pack.tier) },
                    enabled = pack.canAfford,
                    modifier = Modifier.weight(1f),
                ) { Text("${pack.tier.name}\n${pack.tier.gemCost}💎") }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Card collection
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.ownedCards) { card ->
                CardItem(card, state.equippedCount,
                    onEquip = { viewModel.equipCard(card.id) },
                    onUnequip = { viewModel.unequipCard(card.id) },
                    onUpgrade = { viewModel.upgradeCard(card.id) },
                )
            }
        }
    }

    // Pack result dialog
    state.lastPackResult?.let { results ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissPackResult() },
            title = { Text("Pack Opened!") },
            text = {
                Column {
                    results.forEach { r ->
                        val label = if (r.isNew) "🆕 ${formatName(r.type.name)}" else "♻ ${formatName(r.type.name)} → ${r.dustAwarded} Dust"
                        Text(label, color = rarityColor(r.type.rarity))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.dismissPackResult() }) { Text("OK") } },
        )
    }
}

@Composable
private fun CardItem(
    card: CardDisplayInfo, equippedCount: Int,
    onEquip: () -> Unit, onUnequip: () -> Unit, onUpgrade: () -> Unit,
) {
    Card(
        Modifier.fillMaxWidth(),
        border = BorderStroke(2.dp, rarityColor(card.type.rarity)),
        colors = if (card.isEquipped) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        else CardDefaults.cardColors(),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatName(card.type.name), style = MaterialTheme.typography.titleSmall)
                Row {
                    if (card.isMaxLevel) Text("MAX", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    else Text("Lv ${card.level}/${card.type.maxLevel}", style = MaterialTheme.typography.labelMedium)
                    Text(" • ${card.type.rarity.name}", style = MaterialTheme.typography.labelSmall, color = rarityColor(card.type.rarity))
                }
            }
            Text(card.type.effectLv1, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (card.isEquipped) {
                    OutlinedButton(onClick = onUnequip, modifier = Modifier.weight(1f)) { Text("Unequip") }
                } else {
                    Button(onClick = onEquip, enabled = equippedCount < 3, modifier = Modifier.weight(1f)) { Text("Equip") }
                }
                if (!card.isMaxLevel) {
                    Button(onClick = onUpgrade, enabled = card.canAffordUpgrade, modifier = Modifier.weight(1f)) {
                        Text("Upgrade (${card.upgradeDustCost}✨)")
                    }
                }
            }
        }
    }
}

private fun formatName(name: String): String =
    name.split("_").joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }

private fun rarityColor(rarity: CardRarity): Color = when (rarity) {
    CardRarity.COMMON -> Color.Gray
    CardRarity.RARE -> Color(0xFF4488FF)
    CardRarity.EPIC -> Color(0xFFAA44FF)
}
