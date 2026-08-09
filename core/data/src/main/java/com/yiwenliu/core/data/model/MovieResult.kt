package com.yiwenliu.core.data.model

import com.yiwenliu.core.data.util.BACKDROP_SIZE
import com.yiwenliu.core.data.util.asImageUrl
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.network.model.MovieResponse
import com.yiwenliu.core.network.model.MovieResult

fun MovieResult.asExternalModel() = Movie(
    id = id,
    title = title,
    overview = overview,
    releaseDate = releaseDate,
    posterPath = posterPath.asImageUrl(),
    backdropPath = backdropPath.asImageUrl(size = BACKDROP_SIZE),
    voteCount = voteCount,
    voteAverage = voteAverage,
    popularity = popularity,
    genreIds = genreIds,
)

fun MovieResponse.asExternalModel(): List<Movie> = results.map(MovieResult::asExternalModel)
