package com.whitefang.stepsofbabylon.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Displays the privacy policy for Health Connect permissions.
 * Shown when users tap the privacy policy link in the Health Connect permissions screen.
 */
class HealthConnectPermissionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Privacy Policy", style = MaterialTheme.typography.headlineMedium)
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = """
Steps of Babylon uses your device's step counter sensor to track daily walking activity. This data powers all in-game progression.

Health Connect Integration
With your permission, the app reads:
• Step count records — to cross-validate sensor readings and recover missed steps
• Exercise session records — to convert indoor workout minutes into step-equivalent credits (Activity Minute Parity)

You can revoke Health Connect permissions at any time through your device settings.

Data Storage
All game data is stored locally on your device in an encrypted database. Steps of Babylon has no server backend. Your data is never uploaded to any remote server.

No data is sold, traded, or shared with third parties.

Contact: support@whitefanggames.com
                    """.trimIndent()
                )
            }
        }
    }
}
