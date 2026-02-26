package com.duelmath.features.game.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.duelmath.core.navigation.FeatureNavGraph
import com.duelmath.core.navigation.GameRoute
import com.duelmath.core.navigation.LobbyRoute
import com.duelmath.features.game.presentation.screens.GameScreen
import javax.inject.Inject

class GameNavGraph @Inject constructor() : FeatureNavGraph {

    override fun registerGraph(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {
        navGraphBuilder.composable<GameRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<GameRoute>()
            GameScreen(
                sessionId = route.sessionId,
                onGameOver = {
                    navController.navigate(LobbyRoute) {
                        popUpTo(GameRoute(route.sessionId)) { inclusive = true }
                    }
                }
            )
        }
    }
}
