package com.whitefang.stepsofbabylon.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.whitefang.stepsofbabylon.presentation.home.HomeScreen
import com.whitefang.stepsofbabylon.presentation.ui.theme.StepsOfBabylonTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StepsOfBabylonTheme {
                HomeScreen()
            }
        }
    }
}
