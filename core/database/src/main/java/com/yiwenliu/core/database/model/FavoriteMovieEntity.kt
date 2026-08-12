package com.yiwenliu.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_movies")
data class FavoriteMovieEntity(@PrimaryKey val id: Int, val title: String, val posterPath: String, val createdAt: Long)
