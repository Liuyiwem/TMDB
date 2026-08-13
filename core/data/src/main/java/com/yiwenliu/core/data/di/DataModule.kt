package com.yiwenliu.core.data.di

import com.yiwenliu.core.data.repository.FavoriteMovieRepositoryImpl
import com.yiwenliu.core.data.repository.MovieRepositoryImpl
import com.yiwenliu.core.domain.repository.FavoriteMovieRepository
import com.yiwenliu.core.domain.repository.MovieRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    internal abstract fun bindMovieRepository(movieRepositoryImpl: MovieRepositoryImpl): MovieRepository

    @Binds
    internal abstract fun bindFavoriteMovieRepository(
        favoriteMovieRepositoryImpl: FavoriteMovieRepositoryImpl,
    ): FavoriteMovieRepository
}
