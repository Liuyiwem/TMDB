package com.yiwenliu.core.data.test.di

import com.yiwenliu.core.network.api.TmdbApiService
import com.yiwenliu.core.network.di.FlavoredNetworkModule
import com.yiwenliu.core.network.mock.MockTmdbApiService
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [FlavoredNetworkModule::class],
)
internal interface TestNetworkModule {
    @Binds
    @Singleton
    fun bindsTmdbApiService(mock: MockTmdbApiService): TmdbApiService
}
