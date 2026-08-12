package com.yiwenliu.core.data.di

import com.yiwenliu.core.data.repository.FavoriteMovieRepository
import com.yiwenliu.core.data.repository.FavoriteMovieRepositoryImpl
import com.yiwenliu.core.data.repository.MovieRepository
import com.yiwenliu.core.data.repository.MovieRepositoryImpl
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
