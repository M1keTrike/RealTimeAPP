package com.duelmath.features.game.di

import android.content.Context
import androidx.room.Room
import com.duelmath.features.game.data.datasources.local.db.GameDatabase
import com.duelmath.features.game.data.datasources.local.db.GameRoundDao
import com.duelmath.features.game.data.datasources.local.db.GameSessionDao
import com.duelmath.features.game.data.datasources.local.db.RoundResultDao
import com.duelmath.features.game.data.datasources.remote.ws.GameWebSocketDataSource
import com.duelmath.features.game.data.datasources.remote.ws.GameWebSocketDataSourceImpl
import com.duelmath.features.game.data.repositories.GameRepositoryImpl
import com.duelmath.features.game.domain.repositories.GameRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    abstract fun bindGameRepository(
        impl: GameRepositoryImpl
    ): GameRepository

    companion object {

        @Provides
        @Singleton
        fun provideGameDatabase(@ApplicationContext context: Context): GameDatabase =
            Room.databaseBuilder(context, GameDatabase::class.java, "game_database")
                .fallbackToDestructiveMigration()
                .build()

        @Provides
        @Singleton
        fun provideRoundResultDao(db: GameDatabase): RoundResultDao = db.roundResultDao()

        @Provides
        @Singleton
        fun provideGameSessionDao(db: GameDatabase): GameSessionDao = db.gameSessionDao()

        @Provides
        @Singleton
        fun provideGameRoundDao(db: GameDatabase): GameRoundDao = db.gameRoundDao()
    }
}
