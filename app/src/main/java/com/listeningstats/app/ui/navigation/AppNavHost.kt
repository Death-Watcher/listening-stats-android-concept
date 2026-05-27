package com.listeningstats.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.listeningstats.app.domain.*
import com.listeningstats.app.ui.screens.*

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen,
)

val bottomNavItems = listOf(
    BottomNavItem("Dashboard", Icons.Default.Home, Screen.Dashboard),
    BottomNavItem("Top Items", Icons.Default.Star, Screen.TopItems),
    BottomNavItem("Recent", Icons.Default.History, Screen.RecentTracks),
    BottomNavItem("Activity", Icons.Default.BarChart, Screen.Activity),
    BottomNavItem("Settings", Icons.Default.Settings, Screen.Settings),
)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val dashboardViewModel: DashboardViewModel = viewModel()
    val topItemsViewModel: TopItemsViewModel = viewModel()
    val recentTracksViewModel: RecentTracksViewModel = viewModel()
    val activityViewModel: ActivityViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.screen.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onSeeAllTracks = { navController.navigate(Screen.TopItems.route) },
                    onSeeAllArtists = { navController.navigate(Screen.TopItems.route) },
                    onSeeAllAlbums = { navController.navigate(Screen.TopItems.route) },
                    onSeeAllRecent = { navController.navigate(Screen.RecentTracks.route) },
                )
            }
            composable(Screen.TopItems.route) {
                TopItemsScreen(
                    viewModel = topItemsViewModel,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Screen.RecentTracks.route) {
                RecentTracksScreen(
                    viewModel = recentTracksViewModel,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Screen.Activity.route) {
                ActivityScreen(
                    viewModel = activityViewModel,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Screen.ShareCards.route) {
                ShareCardScreen(
                    viewModel = dashboardViewModel,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
