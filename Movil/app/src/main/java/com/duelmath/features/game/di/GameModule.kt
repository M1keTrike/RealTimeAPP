package com.duelmath.features.game.di

import com.duelmath.features.game.data.datasources.local.GameLocalDataSource
import com.duelmath.features.game.data.datasources.local.GameLocalDataSourceImpl
import com.duelmath.features.game.data.datasources.remote.ws.GameWebSocketDataSource
import com.duelmath.features.game.data.datasources.remote.ws.GameWebSocketDataSourceImpl
import com.duelmath.features.game.data.repositories.GameRepositoryImpl
import com.duelmath.features.game.domain.repositories.GameRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GameModule {

    @Binds
    @Singleton
    abstract fun bindGameWebSocketDataSource(
        impl: GameWebSocketDataSourceImpl
    ): GameWebSocketDataSource

    @Binds
    @Singleton
    abstract fun bindGameLocalDataSource(
        impl: GameLocalDataSourceImpl
    ): GameLocalDataSource

    @Binds
    @Singleton
    abstract fun bindGameRepository(
        impl: GameRepositoryImpl
    ): GameRepository
}
