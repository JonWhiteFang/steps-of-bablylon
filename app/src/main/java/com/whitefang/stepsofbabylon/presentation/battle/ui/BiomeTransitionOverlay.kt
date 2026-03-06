package com.whitefang.stepsofbabylon.presentation.battle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.whitefang.stepsofbabylon.domain.model.Biome
import com.whitefang.stepsofbabylon.presentation.battle.biome.BiomeTheme

data class BiomeTransitionInfo(val biome: Biome, val totalSteps: Long)

@Composable
fun BiomeTransitionOverlay(info: BiomeTransitionInfo, onContinue: () -> Unit) {
    val theme = BiomeTheme.forBiome(info.biome)
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(theme.skyColorTop), Color(theme.skyColorBottom)))
        ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Welcome to", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.7f))
            Text(
                info.biome.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text("${info.totalSteps} steps walked", style = MaterialTheme.typography.titleSmall, color = Color.White.copy(alpha = 0.6f))
            Spacer(Modifier.height(24.dp))
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth(0.5f)) { Text("Continue") }
        }
    }
}
