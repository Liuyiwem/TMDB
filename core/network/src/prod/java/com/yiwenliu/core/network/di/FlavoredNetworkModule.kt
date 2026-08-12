package com.yiwenliu.core.network.di

import com.yiwenliu.core.network.api.TmdbApiService
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
    fun providesTmdbApiService(retrofit: Retrofit): TmdbApiService = retrofit.create(TmdbApiService::class.java)
}
