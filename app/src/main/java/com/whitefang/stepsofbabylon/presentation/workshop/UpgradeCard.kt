package com.whitefang.stepsofbabylon.presentation.workshop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.whitefang.stepsofbabylon.presentation.ui.theme.Gold

@Composable
fun UpgradeCard(info: UpgradeDisplayInfo, onClick: () -> Unit) {
    val alpha = when {
        info.isMaxed -> 0.7f
        info.canAfford -> 1f
        else -> 0.5f
    }

    Card(
        onClick = { if (info.canAfford && !info.isMaxed) onClick() },
        modifier = Modifier.fillMaxWidth().alpha(alpha),
        colors = if (info.isMaxed) CardDefaults.cardColors(containerColor = Gold.copy(alpha = 0.15f))
                 else CardDefaults.cardColors(),
    ) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = info.type.name.replace('_', ' '),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = info.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (info.isMaxed) "MAX" else "Lv. ${info.level}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (info.isMaxed) Gold else MaterialTheme.colorScheme.onSurface,
                )
                if (!info.isMaxed) {
                    Text(
                        text = "${info.cost} Steps",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (info.canAfford) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
