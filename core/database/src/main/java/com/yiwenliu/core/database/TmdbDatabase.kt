package com.yiwenliu.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.yiwenliu.core.database.dao.FavoriteMovieDao
import com.yiwenliu.core.database.dao.MovieDao
import com.yiwenliu.core.database.model.FavoriteMovieEntity
import com.yiwenliu.core.database.model.MovieCategoryIndexEntity
import com.yiwenliu.core.database.model.MovieEntity
import com.yiwenliu.core.database.model.MovieRemoteKeyEntity
import com.yiwenliu.core.database.util.MovieConverters

@Database(
    entities = [
        FavoriteMovieEntity::class,
        MovieEntity::class,
        MovieCategoryIndexEntity::class,
        MovieRemoteKeyEntity::class,
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
@TypeConverters(MovieConverters::class)
abstract class TmdbDatabase : RoomDatabase() {
    abstract fun favoriteMovieDao(): FavoriteMovieDao

    abstract fun movieDao(): MovieDao
}
