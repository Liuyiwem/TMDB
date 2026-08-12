package com.yiwenliu.core.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.yiwenliu.core.model.Genre
import com.yiwenliu.core.model.MovieDetail

class MovieDetailPreviewParameterProvider : PreviewParameterProvider<MovieDetail> {
    override val values: Sequence<MovieDetail> =
        sequenceOf(
            MovieDetail(
                id = 533535,
                title = "Deadpool & Wolverine",
                tagline = "Come together.",
                overview =
                "A listless Wade Wilson toils away in civilian life with his days as the morally flexible " +
                    "mercenary, Deadpool, behind him.",
                releaseDate = "2024-07-24",
                posterUrl = "",
                backdropUrl = "",
                runtimeMinutes = 128,
                voteAverage = 7.6,
                voteCount = 5842,
                genres = listOf(Genre(28, "Action"), Genre(35, "Comedy")),
            ),
        )
}
