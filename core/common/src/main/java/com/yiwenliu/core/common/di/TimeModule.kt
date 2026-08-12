package com.yiwenliu.core.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object TimeModule {
    @Provides
    @Singleton
    fun providesTimeProvider(): TimeProvider = TimeProvider { System.currentTimeMillis() }
}
