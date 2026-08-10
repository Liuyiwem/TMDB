package com.yiwenliu.tmdb.splash.di

import com.yiwenliu.tmdb.splash.DefaultSplashConfig
import com.yiwenliu.tmdb.splash.SplashConfig
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SplashModule {
    @Binds
    internal abstract fun bindSplashConfig(defaultSplashConfig: DefaultSplashConfig): SplashConfig
}
