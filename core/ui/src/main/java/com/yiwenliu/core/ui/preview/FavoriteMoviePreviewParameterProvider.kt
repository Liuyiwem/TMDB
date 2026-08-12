package com.yiwenliu.core.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.yiwenliu.core.model.FavoriteMovie

class FavoriteMoviePreviewParameterProvider : PreviewParameterProvider<List<FavoriteMovie>> {
    override val values: Sequence<List<FavoriteMovie>> =
        sequenceOf(
            listOf(
                FavoriteMovie(id = 1, title = "The Shawshank Redemption", posterUrl = ""),
                FavoriteMovie(id = 2, title = "The Godfather", posterUrl = ""),
                FavoriteMovie(id = 3, title = "The Dark Knight", posterUrl = ""),
                FavoriteMovie(id = 4, title = "Schindler's List", posterUrl = ""),
                FavoriteMovie(id = 5, title = "Pulp Fiction", posterUrl = ""),
                FavoriteMovie(id = 6, title = "The Lord of the Rings: The Return of the King", posterUrl = ""),
            ),
        )
}
