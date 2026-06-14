package com.yiwenliu.core.testing.data

import com.yiwenliu.core.model.Movie

val moviesTestData: List<Movie> =
    listOf(
        Movie(
            id = 533535,
            title = "Deadpool & Wolverine",
            overview = "overview",
            releaseDate = "2024-07-24",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            voteAverage = 7.7,
            voteCount = 100,
            popularity = 50.0,
            genreIds = listOf(28, 35),
        ),
        Movie(
            id = 1022789,
            title = "Inside Out 2",
            overview = "overview",
            releaseDate = "2024-06-11",
            posterPath = "/poster2.jpg",
            backdropPath = "/backdrop2.jpg",
            voteAverage = 7.6,
            voteCount = 200,
            popularity = 60.0,
            genreIds = listOf(16, 10751),
        ),
    )
