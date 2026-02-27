package com.duelmath.features.matchmaking.di

import com.duelmath.core.di.ApiRetrofit
import com.duelmath.features.matchmaking.data.datasources.remote.api.MatchmakingApiService
import com.duelmath.features.matchmaking.data.repositories.MatchmakingRepositoryImpl
import com.duelmath.features.matchmaking.domain.repositories.MatchmakingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MatchmakingModuleBinds {
    @Binds
    @Singleton
    abstract fun bindMatchmakingRepository(
        impl: MatchmakingRepositoryImpl
    ): MatchmakingRepository
}

@Module
@InstallIn(SingletonComponent::class)
object MatchmakingModuleProvides {
    @Provides
    @Singleton
    fun provideMatchmakingApiService(
        @ApiRetrofit retrofit: Retrofit
    ): MatchmakingApiService {
        return retrofit.create(MatchmakingApiService::class.java)
    }
}