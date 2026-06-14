package com.yiwenliu.core.model

data class MoviePage(
    val page: Int,
    val movies: List<Movie>,
    val totalPages: Int,
    val totalResults: Int,
)
