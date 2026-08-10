package com.yiwenliu.tmdb.splash.di

import com.yiwenliu.tmdb.splash.FakeSplashConfig
import com.yiwenliu.tmdb.splash.SplashConfig
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [SplashModule::class],
)
internal abstract class TestSplashModule {
    @Binds
    abstract fun bindSplashConfig(fakeSplashConfig: FakeSplashConfig): SplashConfig
}
