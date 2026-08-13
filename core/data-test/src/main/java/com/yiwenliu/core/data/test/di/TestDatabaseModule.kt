package com.yiwenliu.core.data.test.di

import android.content.Context
import androidx.room.Room
import com.yiwenliu.core.database.TmdbDatabase
import com.yiwenliu.core.database.dao.FavoriteMovieDao
import com.yiwenliu.core.database.di.DatabaseModule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class],
)
internal object TestDatabaseModule {
    @Provides
    @Singleton
    fun providesTmdbDatabase(@ApplicationContext context: Context): TmdbDatabase =
        Room.inMemoryDatabaseBuilder(context, TmdbDatabase::class.java).build()

    @Provides
    fun providesFavoriteMovieDao(database: TmdbDatabase): FavoriteMovieDao = database.favoriteMovieDao()
}
