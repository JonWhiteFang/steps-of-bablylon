package com.whitefang.stepsofbabylon.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Stub activity shown when users tap the privacy policy link
 * in the Health Connect permissions screen.
 */
class HealthConnectPermissionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Steps of Babylon uses Health Connect to read your step count and exercise sessions for cross-validation and Activity Minute Parity. Your data stays on-device and is never sent to any server.")
            }
        }
    }
}
