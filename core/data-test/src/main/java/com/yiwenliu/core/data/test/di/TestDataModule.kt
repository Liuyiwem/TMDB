package com.yiwenliu.core.data.test.di

import com.yiwenliu.core.data.di.DataModule
import com.yiwenliu.core.data.repository.MovieRepository
import com.yiwenliu.core.data.test.repository.FakeMovieRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataModule::class],
)
internal interface TestDataModule {
    @Binds
    fun bindMovieRepository(fake: FakeMovieRepository): MovieRepository
}
