package com.duelmath.core.di

import com.duelmath.core.navigation.FeatureNavGraph
import com.duelmath.features.auth.navigation.AuthNavGraph
import com.duelmath.features.matchmaking.navigation.MatchmakingNavGraph
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
        authNavGraph: AuthNavGraph
    ): FeatureNavGraph

    @Binds
    @IntoSet
    abstract fun bindMatchmakingNavGraph(
        matchmakingNavGraph: MatchmakingNavGraph
    ): FeatureNavGraph

}