package com.yiwenliu.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.yiwenliu.core.model.Movie

@Composable
fun MovieItem(
    movie: Movie,
    modifier: Modifier,
) {
    Column(modifier = modifier) {
        DynamicAsyncImage(
            imageUrl = movie.posterPath,
            contentDescription = movie.title,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(2 / 3f)
                    .clip(RoundedCornerShape(12.dp)),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = movie.title,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = movie.releaseDate,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "⭐️ %.1f".format(movie.voteAverage),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Preview()
@Composable
private fun MovieItemPreview(
    @PreviewParameter(MoviePreviewParameterProvider::class) movies: List<Movie>,
) {
    MaterialTheme {
        MovieItem(
            movie = movies[0],
            modifier = Modifier.width(150.dp),
        )
    }
}
