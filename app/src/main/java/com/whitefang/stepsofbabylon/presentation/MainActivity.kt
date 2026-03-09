package com.whitefang.stepsofbabylon.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.health.connect.client.PermissionController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.whitefang.stepsofbabylon.data.healthconnect.HealthConnectClientWrapper
import com.whitefang.stepsofbabylon.presentation.battle.BattleScreen
import com.whitefang.stepsofbabylon.presentation.cards.CardsScreen
import com.whitefang.stepsofbabylon.presentation.economy.CurrencyDashboardScreen
import com.whitefang.stepsofbabylon.presentation.home.HomeScreen
import com.whitefang.stepsofbabylon.presentation.labs.LabsScreen
import com.whitefang.stepsofbabylon.presentation.missions.MissionsScreen
import com.whitefang.stepsofbabylon.presentation.navigation.BottomNavBar
import com.whitefang.stepsofbabylon.presentation.navigation.Screen
import com.whitefang.stepsofbabylon.presentation.settings.NotificationSettingsScreen
import com.whitefang.stepsofbabylon.presentation.stats.StatsScreen
import com.whitefang.stepsofbabylon.presentation.store.StoreScreen
import com.whitefang.stepsofbabylon.presentation.supplies.UnclaimedSuppliesScreen
import com.whitefang.stepsofbabylon.presentation.ui.theme.StepsOfBabylonTheme
import com.whitefang.stepsofbabylon.presentation.weapons.UltimateWeaponScreen
import com.whitefang.stepsofbabylon.presentation.workshop.WorkshopScreen
import com.whitefang.stepsofbabylon.service.StepCounterService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var healthConnectWrapper: HealthConnectClientWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StepsOfBabylonTheme {
                val context = LocalContext.current
                val navController = rememberNavController()

                val hcPermissionLauncher = rememberLauncherForActivityResult(
                    PermissionController.createRequestPermissionResultContract()
                ) { }

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { results ->
                    val activityGranted = results[Manifest.permission.ACTIVITY_RECOGNITION] == true
                    if (activityGranted) {
                        context.startForegroundService(
                            Intent(context, StepCounterService::class.java)
                        )
                        if (healthConnectWrapper.isAvailable()) {
                            hcPermissionLauncher.launch(healthConnectWrapper.getRequiredPermissions())
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    val activityGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACTIVITY_RECOGNITION
                    ) == PackageManager.PERMISSION_GRANTED
                    val notifGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (activityGranted) {
                        context.startForegroundService(
                            Intent(context, StepCounterService::class.java)
                        )
                        if (healthConnectWrapper.isAvailable() && !healthConnectWrapper.hasPermissions()) {
                            hcPermissionLauncher.launch(healthConnectWrapper.getRequiredPermissions())
                        }
                    }

                    if (!activityGranted || !notifGranted) {
                        val needed = buildList {
                            if (!activityGranted) add(Manifest.permission.ACTIVITY_RECOGNITION)
                            if (!notifGranted) add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        permissionLauncher.launch(needed.toTypedArray())
                    }

                    // Deep-link from notifications
                    when (intent?.getStringExtra("navigate_to")) {
                        "supplies" -> navController.navigate(Screen.Supplies.route)
                        "workshop" -> navController.navigate(Screen.Workshop.route)
                        "battle" -> navController.navigate(Screen.Battle.route)
                        "missions" -> navController.navigate(Screen.Missions.route)
                    }
                }

                Scaffold(
                    bottomBar = {
                        val backStackEntry by navController.currentBackStackEntryAsState()
                        if (backStackEntry?.destination?.route != Screen.Battle.route) {
                            BottomNavBar(navController)
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding),
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                onBattleClick = { navController.navigate(Screen.Battle.route) },
                                onSuppliesClick = { navController.navigate(Screen.Supplies.route) },
                                onEconomyClick = { navController.navigate(Screen.Economy.route) },
                                onMissionsClick = { navController.navigate(Screen.Missions.route) },
                                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                                onStoreClick = { navController.navigate(Screen.Store.route) },
                            )
                        }
                        composable(Screen.Workshop.route) {
                            WorkshopScreen(
                                onNavigateToWeapons = { navController.navigate(Screen.Weapons.route) },
                                onNavigateToCards = { navController.navigate(Screen.Cards.route) },
                            )
                        }
                        composable(Screen.Weapons.route) {
                            UltimateWeaponScreen()
                        }
                        composable(Screen.Cards.route) {
                            CardsScreen()
                        }
                        composable(Screen.Battle.route) {
                            BattleScreen(onExitBattle = { navController.popBackStack() })
                        }
                        composable(Screen.Labs.route) {
                            LabsScreen()
                        }
                        composable(Screen.Stats.route) {
                            StatsScreen()
                        }
                        composable(Screen.Supplies.route) {
                            UnclaimedSuppliesScreen()
                        }
                        composable(Screen.Economy.route) {
                            CurrencyDashboardScreen(
                                onStoreClick = { navController.navigate(Screen.Store.route) },
                            )
                        }
                        composable(Screen.Missions.route) {
                            MissionsScreen()
                        }
                        composable(Screen.Settings.route) {
                            NotificationSettingsScreen()
                        }
                        composable(Screen.Store.route) {
                            StoreScreen()
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun PlaceholderScreen(name: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$name — Coming Soon")
    }
}
