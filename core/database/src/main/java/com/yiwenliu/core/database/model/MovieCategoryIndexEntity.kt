package com.yiwenliu.core.database.model

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "movie_category_index",
    primaryKeys = ["category", "movieId"],
    indices = [Index(value = ["category", "position"]), Index(value = ["movieId"])],
)
data class MovieCategoryIndexEntity(val category: String, val movieId: Int, val position: Int)
