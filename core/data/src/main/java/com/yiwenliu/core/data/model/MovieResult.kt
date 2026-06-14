package com.yiwenliu.core.data.model

import com.yiwenliu.core.data.BuildConfig
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.network.model.MovieResult

fun MovieResult.asExternalModel() =
    Movie(
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

private fun String?.asImageUrl(size: String = "w500"): String =
    if (this.isNullOrBlank() || size.isBlank()) {
        ""
    } else {
        "${BuildConfig.IMAGE_URL}${size}$this"
    }
