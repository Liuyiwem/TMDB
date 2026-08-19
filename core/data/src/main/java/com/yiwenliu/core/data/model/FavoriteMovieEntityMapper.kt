package com.yiwenliu.core.data.model

import com.yiwenliu.core.data.util.asImagePath
import com.yiwenliu.core.data.util.asImageUrl
import com.yiwenliu.core.database.model.FavoriteMovieEntity
import com.yiwenliu.core.model.FavoriteMovie

internal fun FavoriteMovieEntity.asExternalModel(): FavoriteMovie = FavoriteMovie(
    id = id,
    title = title,
    posterUrl = posterPath.asImageUrl(),
)

internal fun FavoriteMovie.asEntity(createdAt: Long): FavoriteMovieEntity = FavoriteMovieEntity(
    id = id,
    title = title,
    posterPath = posterUrl?.asImagePath().orEmpty(),
    createdAt = createdAt,
)
