package com.yiwenliu.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yiwenliu.core.database.dao.FavoriteMovieDao
import com.yiwenliu.core.database.model.FavoriteMovieEntity

@Database(
    entities = [FavoriteMovieEntity::class],
    version = 1,
    exportSchema = true,
)
internal abstract class TmdbDatabase : RoomDatabase() {
    abstract fun favoriteMovieDao(): FavoriteMovieDao
}
