package com.duelmath.features.questions.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.duelmath.core.navigation.FeatureNavGraph
import com.duelmath.core.navigation.QuestionsRoute
import com.duelmath.features.questions.presentation.screens.QuestionsScreen
import javax.inject.Inject

class NavigationWrapperQuestions @Inject constructor() : FeatureNavGraph {

    override fun registerGraph(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {
        navGraphBuilder.composable<QuestionsRoute> {
            QuestionsScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
