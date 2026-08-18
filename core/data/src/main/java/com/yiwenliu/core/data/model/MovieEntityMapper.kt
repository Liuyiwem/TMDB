package com.yiwenliu.core.data.model

import com.yiwenliu.core.data.util.BACKDROP_SIZE
import com.yiwenliu.core.data.util.asImageUrl
import com.yiwenliu.core.database.model.MovieEntity
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.network.model.MovieResult

internal fun MovieResult.asEntity() = MovieEntity(
    id = id,
    title = title,
    overview = overview,
    releaseDate = releaseDate,
    posterPath = posterPath,
    backdropPath = backdropPath,
    voteAverage = voteAverage,
    voteCount = voteCount,
    popularity = popularity,
    genreIds = genreIds,
)

internal fun MovieEntity.asExternalModel() = Movie(
    id = id,
    title = title,
    overview = overview,
    releaseDate = releaseDate,
    posterUrl = posterPath.asImageUrl(),
    backdropUrl = backdropPath.asImageUrl(size = BACKDROP_SIZE),
    voteAverage = voteAverage,
    voteCount = voteCount,
    popularity = popularity,
    genreIds = genreIds,
)
