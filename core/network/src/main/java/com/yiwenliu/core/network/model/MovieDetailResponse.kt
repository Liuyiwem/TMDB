package com.yiwenliu.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieDetailResponse(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String = "",
    @SerialName("tagline") val tagline: String = "",
    @SerialName("overview") val overview: String = "",
    @SerialName("release_date") val releaseDate: String = "",
    @SerialName("poster_path") val posterPath: String = "",
    @SerialName("backdrop_path") val backdropPath: String = "",
    @SerialName("runtime") val runtime: Int = 0,
    @SerialName("status") val status: String = "",
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    @SerialName("genres") val genres: List<GenreResult> = emptyList(),
)

@Serializable
data class GenreResult(@SerialName("id") val id: Int, @SerialName("name") val name: String = "")

@Serializable
data class CreditsResponse(
    @SerialName("id") val id: Int = 0,
    @SerialName("cast") val cast: List<CastResult> = emptyList(),
)

@Serializable
data class CastResult(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String = "",
    @SerialName("character") val character: String = "",
    @SerialName("profile_path") val profilePath: String = "",
    @SerialName("order") val order: Int = 0,
)
