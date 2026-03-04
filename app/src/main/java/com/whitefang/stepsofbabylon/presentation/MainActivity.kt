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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.health.connect.client.PermissionController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.whitefang.stepsofbabylon.data.healthconnect.HealthConnectClientWrapper
import com.whitefang.stepsofbabylon.presentation.home.HomeScreen
import com.whitefang.stepsofbabylon.presentation.navigation.BottomNavBar
import com.whitefang.stepsofbabylon.presentation.navigation.Screen
import com.whitefang.stepsofbabylon.presentation.ui.theme.StepsOfBabylonTheme
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

                // Health Connect permission launcher
                val hcPermissionLauncher = rememberLauncherForActivityResult(
                    PermissionController.createRequestPermissionResultContract()
                ) { /* HC permissions granted or denied — either way, app works */ }

                // Standard Android permission launcher
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
                }

                Scaffold(
                    bottomBar = { BottomNavBar(navController) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding),
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(onBattleClick = { navController.navigate(Screen.Battle.route) })
                        }
                        composable(Screen.Workshop.route) {
                            PlaceholderScreen("Workshop")
                        }
                        composable(Screen.Battle.route) {
                            PlaceholderScreen("Battle")
                        }
                        composable(Screen.Labs.route) {
                            PlaceholderScreen("Labs")
                        }
                        composable(Screen.Stats.route) {
                            PlaceholderScreen("Stats")
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
