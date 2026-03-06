package com.whitefang.stepsofbabylon.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.whitefang.stepsofbabylon.presentation.battle.biome.BiomeTheme
import com.whitefang.stepsofbabylon.presentation.ui.theme.Gold
import com.whitefang.stepsofbabylon.presentation.ui.theme.LapisLazuli

@Composable
fun HomeScreen(
    onBattleClick: () -> Unit = {},
    onSuppliesClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val theme = BiomeTheme.forBiome(state.currentBiome)

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(theme.skyColorTop).copy(alpha = 0.3f), Color(theme.skyColorBottom).copy(alpha = 0.15f)))
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TierSelector(
                currentTier = state.currentTier,
                highestUnlockedTier = state.highestUnlockedTier,
                bestWavePerTier = state.bestWavePerTier,
                onSelectTier = viewModel::selectTier,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LapisLazuli.copy(alpha = 0.1f)),
            ) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Today", style = MaterialTheme.typography.labelLarge, color = LapisLazuli)
                    Text("${state.todaySteps} steps", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = LapisLazuli)
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CurrencyItem("Steps", state.stepBalance)
                CurrencyItem("Gems", state.gems)
                CurrencyItem("Power Stones", state.powerStones)
            }

            Text("Best Wave: ${state.bestWave}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)

            if (state.unclaimedDropCount > 0) {
                OutlinedButton(onClick = onSuppliesClick, modifier = Modifier.fillMaxWidth()) {
                    BadgedBox(badge = { Badge { Text("${state.unclaimedDropCount}") } }) {
                        Icon(Icons.Default.Email, contentDescription = "Supplies")
                    }
                    Text("  Unclaimed Supplies", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.weight(1f))

            Button(onClick = onBattleClick, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Gold)) {
                Text("BATTLE", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CurrencyItem(label: String, amount: Long) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$amount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}
