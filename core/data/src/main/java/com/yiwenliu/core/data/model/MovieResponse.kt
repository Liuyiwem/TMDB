package com.yiwenliu.core.data.model

import com.yiwenliu.core.model.MoviePage
import com.yiwenliu.core.network.model.MovieResponse

fun MovieResponse.asExternalModel() = MoviePage(
    page = page,
    movies = results.map { it.asExternalModel() },
    totalPages = totalPages,
    totalResults = totalResults,
)
