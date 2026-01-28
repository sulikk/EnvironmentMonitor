package com.example.environmentmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.environmentmonitor.ui.theme.EnvironmentMonitorTheme
import kotlinx.serialization.Serializable

@Serializable object DashboardRoute
@Serializable object HistoryRoute

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EnvironmentMonitorTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = DashboardRoute) {
                    composable<DashboardRoute> {
                        DashboardScreen(onNavigateToHistory = {
                            navController.navigate(HistoryRoute)
                        })
                    }
                    composable<HistoryRoute> {
                        HistoryScreen()
                    }
                }
            }
        }
    }
}