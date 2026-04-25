package com.kangraemin.pictalk.data.di

import com.kangraemin.pictalk.data.repository.ArasaacRepositoryImpl
import com.kangraemin.pictalk.data.repository.GemmaRepositoryImpl
import com.kangraemin.pictalk.data.repository.ModelRepositoryImpl
import com.kangraemin.pictalk.domain.repository.ArasaacRepository
import com.kangraemin.pictalk.domain.repository.GemmaRepository
import com.kangraemin.pictalk.domain.repository.ModelRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds @Singleton
    abstract fun bindArasaacRepository(impl: ArasaacRepositoryImpl): ArasaacRepository

    @Binds @Singleton
    abstract fun bindGemmaRepository(impl: GemmaRepositoryImpl): GemmaRepository

    @Binds @Singleton
    abstract fun bindModelRepository(impl: ModelRepositoryImpl): ModelRepository

    companion object {
        @Provides @Singleton
        fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.MINUTES)
            .build()
    }
}
