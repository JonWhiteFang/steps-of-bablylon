package com.whitefang.stepsofbabylon.presentation.battle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.whitefang.stepsofbabylon.presentation.battle.ui.InRoundUpgradeMenu
import com.whitefang.stepsofbabylon.presentation.battle.ui.PauseOverlay
import com.whitefang.stepsofbabylon.presentation.battle.ui.PostRoundOverlay

@Composable
fun BattleScreen(
    onExitBattle: () -> Unit,
    viewModel: BattleViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val surfaceView = remember { GameSurfaceView(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val roundActive = state.roundEndState == null

    LaunchedEffect(surfaceView) { viewModel.startPollingEngine(surfaceView.engine, surfaceView) }
    LaunchedEffect(state.speedMultiplier) { surfaceView.setSpeedMultiplier(state.speedMultiplier) }
    LaunchedEffect(state.isPaused) { surfaceView.setPaused(state.isPaused) }
    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) surfaceView.configure(viewModel.resolvedStats, viewModel.tier, emptyMap())
    }

    // Auto-pause on background
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) viewModel.pause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { surfaceView }, modifier = Modifier.fillMaxSize())

        // Top-left: wave info + cash
        Column(Modifier.align(Alignment.TopStart).padding(start = 16.dp, top = 80.dp)) {
            Text("Wave ${state.currentWave} · ${state.enemyCount} enemies", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Text(state.wavePhase.lowercase().replaceFirstChar { it.uppercase() }, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            Text("$${state.cash}", color = Color(0xFFD4A843), style = MaterialTheme.typography.titleSmall)
        }

        // Top-right: exit (quit round)
        if (roundActive) {
            IconButton(onClick = { viewModel.quitRound() }, modifier = Modifier.align(Alignment.TopEnd).padding(end = 8.dp, top = 72.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quit round", tint = Color.White)
            }
        }

        // Bottom: speed controls + pause + upgrade toggle (hidden when round over)
        if (roundActive) {
            Row(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf(1f, 2f, 4f).forEach { speed ->
                    if (state.speedMultiplier == speed) {
                        Button(onClick = {}) { Text("${speed.toInt()}x") }
                    } else {
                        FilledTonalButton(
                            onClick = { viewModel.setSpeed(speed) },
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                        ) { Text("${speed.toInt()}x", color = Color.White) }
                    }
                }
                FilledTonalButton(
                    onClick = { viewModel.togglePause() },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (state.isPaused) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.2f),
                    ),
                ) { Text(if (state.isPaused) "▶" else "⏸", color = Color.White) }

                FilledTonalButton(
                    onClick = { viewModel.toggleUpgradeMenu() },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (state.showUpgradeMenu) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.2f),
                    ),
                ) { Text("⬆", color = Color.White) }
            }
        }

        // Upgrade menu overlay
        if (state.showUpgradeMenu && roundActive) {
            Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 72.dp)) {
                InRoundUpgradeMenu(
                    cash = state.cash,
                    inRoundLevels = state.inRoundLevels,
                    onPurchase = viewModel::purchaseInRoundUpgrade,
                    onDismiss = viewModel::toggleUpgradeMenu,
                )
            }
        }

        // Pause overlay
        if (state.isPaused && roundActive) {
            PauseOverlay(
                onResume = { viewModel.togglePause() },
                onQuitRound = { viewModel.quitRound() },
            )
        }

        // Post-round overlay
        state.roundEndState?.let { endState ->
            PostRoundOverlay(
                state = endState,
                onPlayAgain = { viewModel.playAgain() },
                onReturnToWorkshop = onExitBattle,
            )
        }
    }
}
