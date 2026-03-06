package com.whitefang.stepsofbabylon.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.whitefang.stepsofbabylon.presentation.ui.theme.Gold
import com.whitefang.stepsofbabylon.presentation.ui.theme.LapisLazuli

@Composable
fun HomeScreen(
    onBattleClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Tier selector
        TierSelector(
            currentTier = state.currentTier,
            highestUnlockedTier = state.highestUnlockedTier,
            bestWavePerTier = state.bestWavePerTier,
            onSelectTier = viewModel::selectTier,
        )

        // Today's steps card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LapisLazuli.copy(alpha = 0.1f)),
        ) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Today", style = MaterialTheme.typography.labelLarge, color = LapisLazuli)
                Text(
                    text = "${state.todaySteps} steps",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = LapisLazuli,
                )
            }
        }

        // Currency row
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            CurrencyItem("Steps", state.stepBalance)
            CurrencyItem("Gems", state.gems)
            CurrencyItem("Power Stones", state.powerStones)
        }

        // Best wave
        Text(
            text = "Best Wave: ${state.bestWave}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.weight(1f))

        // Battle button
        Button(
            onClick = onBattleClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Gold),
        ) {
            Text("BATTLE", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
