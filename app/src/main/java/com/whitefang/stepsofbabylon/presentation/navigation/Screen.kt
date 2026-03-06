package com.whitefang.stepsofbabylon.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Workshop : Screen("workshop", "Workshop", Icons.Default.Build)
    data object Battle : Screen("battle", "Battle", Icons.Default.PlayArrow)
    data object Labs : Screen("labs", "Labs", Icons.Default.Search)
    data object Stats : Screen("stats", "Stats", Icons.Default.Star)
    data object Weapons : Screen("weapons", "Weapons", Icons.Default.Star)
    data object Cards : Screen("cards", "Cards", Icons.Default.Star)

    companion object {
        val items = listOf(Home, Workshop, Battle, Labs, Stats)
    }
}
