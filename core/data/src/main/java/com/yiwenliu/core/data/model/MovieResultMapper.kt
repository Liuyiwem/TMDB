package com.yiwenliu.core.data.model

import com.yiwenliu.core.data.util.BACKDROP_SIZE
import com.yiwenliu.core.data.util.asImageUrl
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.network.model.MovieResponse
import com.yiwenliu.core.network.model.MovieResult

internal fun MovieResult.asExternalModel() = Movie(
    id = id,
    title = title,
    overview = overview,
    releaseDate = releaseDate,
    posterUrl = posterPath.asImageUrl(),
    backdropUrl = backdropPath.asImageUrl(size = BACKDROP_SIZE),
    voteCount = voteCount,
    voteAverage = voteAverage,
    popularity = popularity,
    genreIds = genreIds,
)

internal fun MovieResponse.asExternalModel(): List<Movie> = results.map(MovieResult::asExternalModel)
