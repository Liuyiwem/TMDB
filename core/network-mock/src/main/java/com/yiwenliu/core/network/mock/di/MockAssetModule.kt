package com.yiwenliu.core.network.mock.di

import android.content.Context
import com.yiwenliu.core.network.mock.MockAssetManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object MockAssetModule {
    @Provides
    @Singleton
    fun providesMockAssetManager(@ApplicationContext context: Context): MockAssetManager =
        MockAssetManager(context.assets::open)
}
