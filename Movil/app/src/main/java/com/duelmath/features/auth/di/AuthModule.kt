package com.duelmath.features.auth.di

import com.duelmath.core.di.ApiRetrofit
import com.duelmath.features.auth.data.datasources.local.AuthLocalDataSource
import com.duelmath.features.auth.data.datasources.local.AuthLocalDataSourceImpl
import com.duelmath.features.auth.data.datasources.remote.api.AuthApiService
import com.duelmath.features.auth.data.repositories.AuthRepositoryImpl
import com.duelmath.features.auth.domain.repositories.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModuleBinds {

    @Binds
    @Singleton
    abstract fun bindAuthLocalDataSource(
        impl: AuthLocalDataSourceImpl
    ): AuthLocalDataSource

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AuthModuleProvides {

    @Provides
    @Singleton
    fun provideAuthApiService(
        @ApiRetrofit retrofit: Retrofit
    ): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
}