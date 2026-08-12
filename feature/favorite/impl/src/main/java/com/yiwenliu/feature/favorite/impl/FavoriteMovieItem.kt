package com.yiwenliu.feature.favorite.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.yiwenliu.core.model.FavoriteMovie
import com.yiwenliu.core.ui.component.DynamicAsyncImage
import com.yiwenliu.core.ui.preview.FavoriteMoviePreviewParameterProvider

@Composable
internal fun FavoriteMovieItem(
    movie: FavoriteMovie,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag(FavoriteTestTags.item(movie.id)),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            DynamicAsyncImage(
                imageUrl = movie.posterUrl,
                contentDescription = movie.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2 / 3f)
                    .clip(RoundedCornerShape(12.dp)),
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
            ) {
                IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier.testTag(FavoriteTestTags.remove(movie.id)),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = stringResource(R.string.remove_favorite),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = movie.title,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoriteMovieItemPreview(
    @PreviewParameter(FavoriteMoviePreviewParameterProvider::class) movies: List<FavoriteMovie>,
) {
    MaterialTheme {
        FavoriteMovieItem(
            movie = movies.first(),
            onClick = {},
            onRemoveClick = {},
            modifier = Modifier.width(150.dp),
        )
    }
}
