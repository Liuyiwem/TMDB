package com.yiwenliu.core.database.di

import android.content.Context
import androidx.room.Room
import com.yiwenliu.core.database.TmdbDatabase
import com.yiwenliu.core.database.dao.FavoriteMovieDao
import com.yiwenliu.core.database.dao.MovieDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun providesTmdbDatabase(@ApplicationContext context: Context): TmdbDatabase = Room.databaseBuilder(
        context,
        TmdbDatabase::class.java,
        DATABASE_NAME,
    ).build()

    @Provides
    fun providesFavoriteMovieDao(database: TmdbDatabase): FavoriteMovieDao = database.favoriteMovieDao()

    @Provides
    fun providesMovieDao(database: TmdbDatabase): MovieDao = database.movieDao()

    private const val DATABASE_NAME = "tmdb-database"
}
