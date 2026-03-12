package com.whitefang.stepsofbabylon.presentation.battle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whitefang.stepsofbabylon.presentation.battle.UWSlotInfo

@Composable
fun UltimateWeaponBar(slots: List<UWSlotInfo>, onActivate: (Int) -> Unit) {
    if (slots.isEmpty()) return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        slots.forEachIndexed { index, slot ->
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
                    .background(if (slot.isReady) Color(0xFF6A5ACD) else Color(0xFF2A2A3E))
                    .clickable(enabled = slot.isReady) { onActivate(index) }
                    .semantics {
                        contentDescription = if (slot.isReady) "Activate ${slot.typeName}"
                        else "${slot.typeName} on cooldown, ${slot.cooldownRemaining.toInt()} seconds"
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (slot.isReady) {
                    Text(slot.typeName.take(2), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                } else {
                    Text("${slot.cooldownRemaining.toInt()}", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }
    }
}
