package com.whitefang.stepsofbabylon.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    secondary = LapisLazuli,
    tertiary = SandStone,
    background = DeepBronze,
    surface = DeepBronze,
    onPrimary = Ivory,
    onBackground = Ivory,
    onSurface = Ivory,
)

@Composable
fun StepsOfBabylonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content,
    )
}
