package com.yiwenliu.core.data.model

import com.yiwenliu.core.data.util.BACKDROP_SIZE
import com.yiwenliu.core.data.util.PROFILE_SIZE
import com.yiwenliu.core.data.util.asImageUrl
import com.yiwenliu.core.model.CastMember
import com.yiwenliu.core.model.Genre
import com.yiwenliu.core.model.MovieDetail
import com.yiwenliu.core.network.model.CastResult
import com.yiwenliu.core.network.model.GenreResult
import com.yiwenliu.core.network.model.MovieDetailResponse

fun MovieDetailResponse.asExternalModel() = MovieDetail(
    id = id,
    title = title,
    tagline = tagline,
    overview = overview,
    releaseDate = releaseDate,
    posterPath = posterPath.asImageUrl(),
    backdropPath = backdropPath.asImageUrl(size = BACKDROP_SIZE),
    runtimeMinutes = runtime,
    voteAverage = voteAverage,
    voteCount = voteCount,
    genres = genres.map(GenreResult::asExternalModel),
)

fun GenreResult.asExternalModel() = Genre(
    id = id,
    name = name,
)

fun CastResult.asExternalModel() = CastMember(
    id = id,
    name = name,
    character = character,
    profilePath = profilePath?.asImageUrl(size = PROFILE_SIZE),
)
