package com.duelmath.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.duelmath.core.navigation.*
@Composable
fun DuelMathNavigationWrapper(navGraphs: Set<FeatureNavGraph>) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.hasRoute(LobbyRoute::class) == true ||
            currentDestination?.hasRoute(ProfileRoute::class) == true

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = LoginRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            navGraphs.forEach { graph ->
                graph.registerGraph(this, navController)
            }

            composable<LobbyRoute> {
                Text("Bienvenido al Lobby del Juego", modifier = Modifier.fillMaxSize())
            }
        }
    }
}