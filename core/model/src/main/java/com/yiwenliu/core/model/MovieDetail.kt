package com.yiwenliu.core.model

data class MovieDetail(
    val id: Int,
    val title: String,
    val tagline: String,
    val overview: String,
    val releaseDate: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val runtimeMinutes: Int,
    val voteAverage: Double,
    val voteCount: Int,
    val genres: List<Genre>,
)

data class Genre(val id: Int, val name: String)

data class CastMember(val id: Int, val name: String, val character: String, val profileUrl: String?)

data class MovieDetailBundle(
    val detail: MovieDetail,
    val cast: List<CastMember>,
    val recommendations: List<Movie>,
    val isFavorite: Boolean = false,
)

fun MovieDetail.asFavoriteMovie(): FavoriteMovie = FavoriteMovie(
    id = id,
    title = title,
    posterUrl = posterUrl,
)
