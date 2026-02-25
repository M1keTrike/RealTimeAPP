package com.duelmath.features.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.duelmath.core.navigation.FeatureNavGraph
import com.duelmath.core.navigation.LoginRoute
import com.duelmath.core.navigation.LobbyRoute
import com.duelmath.core.navigation.RegisterRoute
import com.duelmath.features.auth.presentation.screens.LoginScreen
import com.duelmath.features.auth.presentation.screens.RegisterScreen
import javax.inject.Inject

class AuthNavGraph @Inject constructor() : FeatureNavGraph {

    override fun registerGraph(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {

        navGraphBuilder.composable<LoginRoute> {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(RegisterRoute) },
                onLoginSuccess = {
                    navController.navigate(LobbyRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                }
            )
        }

        navGraphBuilder.composable<RegisterRoute> {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = { navController.popBackStack() }
            )
        }
    }
}