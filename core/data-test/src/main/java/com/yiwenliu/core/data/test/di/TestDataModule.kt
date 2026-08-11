package com.yiwenliu.core.data.test.di

import com.yiwenliu.core.data.di.DataModule
import com.yiwenliu.core.data.repository.FavoriteMovieRepository
import com.yiwenliu.core.data.repository.MovieRepository
import com.yiwenliu.core.data.test.repository.FakeFavoriteMovieRepository
import com.yiwenliu.core.data.test.repository.FakeMovieRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataModule::class],
)
internal interface TestDataModule {
    @Binds
    fun bindMovieRepository(fake: FakeMovieRepository): MovieRepository

    @Binds
    @Singleton
    fun bindFavoriteMovieRepository(fake: FakeFavoriteMovieRepository): FavoriteMovieRepository
}
