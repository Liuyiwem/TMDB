package com.yiwenliu.core.testing.data

import com.yiwenliu.core.model.CastMember
import com.yiwenliu.core.model.Genre
import com.yiwenliu.core.model.MovieDetail

val movieDetailTestData: MovieDetail =
    MovieDetail(
        id = 533535,
        title = "Deadpool & Wolverine",
        tagline = "Come together.",
        overview = "overview",
        releaseDate = "2024-07-24",
        posterUrl = "/poster.jpg",
        backdropUrl = "/backdrop.jpg",
        runtimeMinutes = 128,
        voteAverage = 7.6,
        voteCount = 5842,
        genres = listOf(Genre(28, "Action"), Genre(35, "Comedy")),
    )

val castTestData: List<CastMember> =
    listOf(
        CastMember(
            id = 10859,
            name = "Ryan Reynolds",
            character = "Wade Wilson / Deadpool",
            profileUrl = "/ryan.jpg",
        ),
        CastMember(
            id = 6968,
            name = "Hugh Jackman",
            character = "Logan / Wolverine",
            profileUrl = null,
        ),
    )
