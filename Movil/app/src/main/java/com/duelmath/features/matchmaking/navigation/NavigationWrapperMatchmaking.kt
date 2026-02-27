package com.duelmath.features.matchmaking.navigation

import android.util.Log
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.duelmath.core.navigation.FeatureNavGraph
import com.duelmath.core.navigation.GameRoute
import com.duelmath.core.navigation.LoginRoute
import com.duelmath.core.navigation.LobbyRoute
import com.duelmath.core.navigation.QuestionsRoute
import com.duelmath.features.matchmaking.presentation.screens.LobbyScreen
import javax.inject.Inject

class NavigationWrapperMatchmaking @Inject constructor() : FeatureNavGraph {

    override fun registerGraph(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {

        navGraphBuilder.composable<LobbyRoute> {
            Log.d("NavMatchmaking", "composable<LobbyRoute> — rendering LobbyScreen")
            LobbyScreen(
                onMatchFound = { sessionId ->
                    Log.d("NavMatchmaking", "onMatchFound — navigating to GameRoute sessionId=$sessionId")
                    navController.navigate(GameRoute(sessionId)) {
                        launchSingleTop = true
                    }
                },
                onOpenQuestionsAdmin = {
                    navController.navigate(QuestionsRoute)
                },
                onLogout = {
                    navController.navigate(LoginRoute) {
                        popUpTo<LobbyRoute> { inclusive = true }
                    }
                }
            )
        }
    }
}
