package com.yiwenliu.core.data.model

import com.yiwenliu.core.data.util.asImageUrl
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.network.model.MovieResult

fun MovieResult.asExternalModel() = Movie(
    id = id,
    title = title,
    overview = overview,
    releaseDate = releaseDate,
    posterPath = posterPath.asImageUrl(),
    backdropPath = backdropPath,
    voteCount = voteCount,
    voteAverage = voteAverage,
    popularity = popularity,
    genreIds = genreIds,
)
