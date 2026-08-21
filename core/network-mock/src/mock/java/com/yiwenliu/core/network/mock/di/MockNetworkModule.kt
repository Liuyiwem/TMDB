package com.yiwenliu.core.network.mock.di

import com.yiwenliu.core.network.api.TmdbApiService
import com.yiwenliu.core.network.mock.MockTmdbApiService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface MockNetworkModule {
    @Binds
    @Singleton
    fun bindsTmdbApiService(mock: MockTmdbApiService): TmdbApiService
}
