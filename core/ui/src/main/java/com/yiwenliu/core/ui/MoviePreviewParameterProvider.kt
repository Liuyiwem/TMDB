package com.yiwenliu.core.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.yiwenliu.core.model.Movie

class MoviePreviewParameterProvider : PreviewParameterProvider<List<Movie>> {
    override val values: Sequence<List<Movie>> =
        sequenceOf(
            listOf(
                Movie(
                    id = 1,
                    title = "The Shawshank Redemption",
                    overview = "A banker convicted of uxoricide...",
                    releaseDate = "1994-09-23",
                    posterPath = "",
                    backdropPath = "",
                    voteAverage = 9.3,
                    voteCount = 27000,
                    popularity = 100.0,
                    genreIds = listOf(18, 80),
                ),
                Movie(
                    id = 2,
                    title = "The Godfather",
                    overview = "The aging patriarch of an organized crime dynasty transfers control to his reluctant son.",
                    releaseDate = "1972-03-24",
                    posterPath = "",
                    backdropPath = "",
                    voteAverage = 9.2,
                    voteCount = 19000,
                    popularity = 95.0,
                    genreIds = listOf(18, 80),
                ),
                Movie(
                    id = 3,
                    title = "The Dark Knight",
                    overview =
                    "When the menace known as the Joker wreaks havoc on Gotham City, Batman must accept one of the greatest tests.",
                    releaseDate = "2008-07-18",
                    posterPath = "",
                    backdropPath = "",
                    voteAverage = 9.0,
                    voteCount = 32000,
                    popularity = 98.0,
                    genreIds = listOf(28, 80, 18),
                ),
                Movie(
                    id = 4,
                    title = "Schindler's List",
                    overview =
                    "In German-occupied Poland during World War II, industrialist Oskar Schindler saves Jewish refugees.",
                    releaseDate = "1993-12-15",
                    posterPath = "",
                    backdropPath = "",
                    voteAverage = 9.0,
                    voteCount = 15000,
                    popularity = 88.0,
                    genreIds = listOf(18, 36),
                ),
                Movie(
                    id = 5,
                    title = "Pulp Fiction",
                    overview =
                    "The lives of two mob hitmen, a boxer, a gangster and his wife intertwine in four tales of violence and redemption.",
                    releaseDate = "1994-10-14",
                    posterPath = "",
                    backdropPath = "",
                    voteAverage = 8.9,
                    voteCount = 26000,
                    popularity = 92.0,
                    genreIds = listOf(53, 80),
                ),
                Movie(
                    id = 6,
                    title = "The Lord of the Rings: The Return of the King",
                    overview = "Gandalf and Aragorn lead the World of Men against Sauron's army to draw his gaze from Frodo and Sam.",
                    releaseDate = "2003-12-17",
                    posterPath = "",
                    backdropPath = "",
                    voteAverage = 8.9,
                    voteCount = 23000,
                    popularity = 91.0,
                    genreIds = listOf(12, 14, 28),
                ),
            ),
        )
}
