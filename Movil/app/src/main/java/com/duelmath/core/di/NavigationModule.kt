package com.duelmath.core.di

import com.duelmath.core.navigation.FeatureNavGraph
import com.duelmath.features.auth.navigation.NavigationWrapperAuth
import com.duelmath.features.game.navigation.NavigationWrapperGame
import com.duelmath.features.matchmaking.navigation.NavigationWrapperMatchmaking
import com.duelmath.features.questions.navigation.NavigationWrapperQuestions
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class NavigationModule {

    @Binds
    @IntoSet
    abstract fun bindAuthNavGraph(
        navigationWrapperAuth: NavigationWrapperAuth
    ): FeatureNavGraph

    @Binds
    @IntoSet
    abstract fun bindMatchmakingNavGraph(
        navigationWrapperMatchmaking: NavigationWrapperMatchmaking
    ): FeatureNavGraph

    @Binds
    @IntoSet
    abstract fun bindGameNavGraph(
        navigationWrapperGame: NavigationWrapperGame
    ): FeatureNavGraph

    @Binds
    @IntoSet
    abstract fun bindQuestionsNavGraph(
        navigationWrapperQuestions: NavigationWrapperQuestions
    ): FeatureNavGraph
}
