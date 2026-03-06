package com.whitefang.stepsofbabylon.presentation.battle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.whitefang.stepsofbabylon.domain.model.OverdriveType

@Composable
fun OverdriveMenu(
    stepBalance: Long,
    onSelect: (OverdriveType) -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(0.9f).background(Color(0xEE1A1A2E), RoundedCornerShape(12.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Step Overdrive", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        OverdriveType.entries.forEach { type ->
            val affordable = stepBalance >= type.stepCost
            Card(
                modifier = Modifier.fillMaxWidth().clickable(enabled = affordable) { onSelect(type) },
                colors = CardDefaults.cardColors(containerColor = if (affordable) Color(0xFF2D2D4E) else Color(0xFF1A1A2E)),
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(type.name.replace('_', ' '), color = if (affordable) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                        Text(type.description, color = if (affordable) Color.White.copy(alpha = 0.7f) else Color.Gray.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
                    }
                    Text("${type.stepCost} Steps", color = if (affordable) Color(0xFFD4A843) else Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }
        TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Cancel", color = Color.White.copy(alpha = 0.6f)) }
    }
}
