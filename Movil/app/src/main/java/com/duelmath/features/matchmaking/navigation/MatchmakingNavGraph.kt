package com.duelmath.features.matchmaking.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.duelmath.core.navigation.FeatureNavGraph
import com.duelmath.core.navigation.GameRoute
import com.duelmath.core.navigation.LobbyRoute
import com.duelmath.features.matchmaking.presentation.screens.LobbyScreen
import javax.inject.Inject

class MatchmakingNavGraph @Inject constructor() : FeatureNavGraph {

    override fun registerGraph(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {

        navGraphBuilder.composable<LobbyRoute> {
            LobbyScreen(
                onMatchFound = { sessionId ->
                    navController.navigate(GameRoute(sessionId))
                }
            )
        }
    }
}