package com.yiwenliu.core.network.di

import com.yiwenliu.core.network.api.TMDBApiService
import com.yiwenliu.core.network.mock.MockTMDBApiService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface FlavoredNetworkModule {
    @Binds
    @Singleton
    fun bindsTMDBApiService(mock: MockTMDBApiService): TMDBApiService
}
