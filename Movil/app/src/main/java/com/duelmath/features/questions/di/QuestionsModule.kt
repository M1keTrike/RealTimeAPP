package com.duelmath.features.questions.di

import com.duelmath.core.di.ApiRetrofit
import com.duelmath.features.questions.data.datasources.remote.api.QuestionsApiService
import com.duelmath.features.questions.data.repositories.QuestionsRepositoryImpl
import com.duelmath.features.questions.domain.repositories.QuestionsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class QuestionsModuleBinds {
    @Binds
    @Singleton
    abstract fun bindQuestionsRepository(
        impl: QuestionsRepositoryImpl,
    ): QuestionsRepository
}

@Module
@InstallIn(SingletonComponent::class)
object QuestionsModuleProvides {
    @Provides
    @Singleton
    fun provideQuestionsApiService(
        @ApiRetrofit retrofit: Retrofit,
    ): QuestionsApiService {
        return retrofit.create(QuestionsApiService::class.java)
    }
}