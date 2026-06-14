package com.yiwenliu.core.network.di

import com.yiwenliu.core.network.api.TMDBApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FlavoredNetworkModule {
    @Provides
    @Singleton
    fun providesTMDBApiService(retrofit: Retrofit): TMDBApiService = retrofit.create(TMDBApiService::class.java)
}
