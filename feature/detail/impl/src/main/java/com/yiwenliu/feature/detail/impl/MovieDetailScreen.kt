package com.yiwenliu.feature.detail.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yiwenliu.core.model.CastMember
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieDetail
import com.yiwenliu.core.ui.LocalSnackbarHostState
import com.yiwenliu.core.ui.component.DynamicAsyncImage
import com.yiwenliu.core.ui.component.MovieItem
import com.yiwenliu.core.ui.component.TmdbIconToggleButton
import com.yiwenliu.core.ui.component.TmdbMessageDialog
import com.yiwenliu.core.ui.preview.CastPreviewParameterProvider
import com.yiwenliu.core.ui.preview.MovieDetailPreviewParameterProvider
import com.yiwenliu.core.ui.preview.MoviePreviewParameterProvider
import com.yiwenliu.core.ui.util.ObserveAsEvents

@Composable
internal fun MovieDetailRoot(
    movieId: Int,
    onTitleLoaded: (String) -> Unit,
    onMovieClick: (Int, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MovieDetailViewModel = hiltViewModel<MovieDetailViewModel, MovieDetailViewModel.Factory>(
        key = movieId.toString(),
        creationCallback = { factory -> factory.create(movieId) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current
    val context = LocalContext.current
    val loadedTitle = state.detail?.title

    LaunchedEffect(loadedTitle) {
        if (!loadedTitle.isNullOrBlank()) onTitleLoaded(loadedTitle)
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is MovieDetailEvent.ShowError -> snackbarHostState.showSnackbar(event.message.asString(context))
        }
    }

    MovieDetailScreen(
        state = state,
        modifier = modifier,
        onAction = viewModel::onAction,
        onRecommendationClick = onMovieClick,
    )

    state.error?.let { error ->
        TmdbMessageDialog(
            message = error.asString(),
            confirmText = stringResource(com.yiwenliu.core.ui.R.string.ok),
            onConfirm = onBack,
        )
    }
}

@Composable
internal fun MovieDetailScreen(
    state: MovieDetailUiState,
    onAction: (MovieDetailAction) -> Unit,
    onRecommendationClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when {
            state.isLoading -> CircularProgressIndicator(
                modifier = Modifier.testTag(DetailTestTags.LOADING),
            )

            state.detail != null -> MovieDetailContent(
                detail = state.detail,
                cast = state.cast,
                recommendations = state.recommendations,
                isFavorite = state.isFavorite,
                onFavoriteToggle = { checked ->
                    onAction(MovieDetailAction.OnFavoriteToggle(checked))
                },
                onRecommendationClick = onRecommendationClick,
            )
        }
    }
}

@Composable
private fun MovieDetailContent(
    detail: MovieDetail,
    cast: List<CastMember>,
    recommendations: List<Movie>,
    isFavorite: Boolean,
    onFavoriteToggle: (Boolean) -> Unit,
    onRecommendationClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag(DetailTestTags.CONTENT),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            MovieDetailHeader(
                detail = detail,
                isFavorite = isFavorite,
                onFavoriteToggle = onFavoriteToggle,
            )
        }

        if (detail.overview.isNotBlank()) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SectionTitle(text = stringResource(R.string.overview))
                    Text(
                        text = detail.overview,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        if (cast.isNotEmpty()) {
            item {
                SectionTitle(
                    text = stringResource(R.string.cast),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.testTag(DetailTestTags.CAST),
                ) {
                    items(items = cast, key = CastMember::id) { member ->
                        CastItem(member = member)
                    }
                }
            }
        }

        if (recommendations.isNotEmpty()) {
            item {
                SectionTitle(
                    text = stringResource(R.string.recommendations),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.testTag(DetailTestTags.RECOMMENDATIONS),
                ) {
                    items(items = recommendations, key = Movie::id) { movie ->
                        MovieItem(
                            movie = movie,
                            onClick = { onRecommendationClick(movie.id, movie.title) },
                            modifier = Modifier.width(120.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieDetailHeader(
    detail: MovieDetail,
    isFavorite: Boolean,
    onFavoriteToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        DynamicAsyncImage(
            imageUrl = detail.backdropUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3 / 2f),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        1f to MaterialTheme.colorScheme.surface,
                    ),
                ),
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            DynamicAsyncImage(
                imageUrl = detail.posterUrl,
                contentDescription = detail.title,
                modifier = Modifier
                    .width(110.dp)
                    .aspectRatio(2 / 3f)
                    .clip(RoundedCornerShape(12.dp)),
            )
            MovieDetailTitles(
                detail = detail,
                modifier = Modifier.weight(1f),
            )
            TmdbIconToggleButton(
                checked = isFavorite,
                onCheckedChange = onFavoriteToggle,
                modifier = Modifier.testTag(DetailTestTags.FAVORITE),
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.FavoriteBorder,
                        contentDescription = stringResource(R.string.add_to_favorites),
                    )
                },
                checkedIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = stringResource(R.string.remove_from_favorites),
                    )
                },
            )
        }
    }
}

@Composable
private fun MovieDetailTitles(detail: MovieDetail, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (detail.tagline.isNotBlank()) {
            Text(
                text = detail.tagline,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = detail.metaLine(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "⭐️ %.1f (%d)".format(detail.voteAverage, detail.voteCount),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun MovieDetail.metaLine(): String {
    val runtimeText = stringResource(R.string.runtime_minutes, runtimeMinutes)
    val parts = buildList {
        if (releaseDate.isNotBlank()) add(releaseDate)
        if (runtimeMinutes > 0) add(runtimeText)
        if (genres.isNotEmpty()) add(genres.joinToString { it.name })
    }
    return parts.joinToString(" · ")
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier,
    )
}

@Composable
private fun CastItem(member: CastMember, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.width(96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        DynamicAsyncImage(
            imageUrl = member.profileUrl,
            contentDescription = member.name,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape),
        )
        Text(
            text = member.name,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Text(
            text = member.character,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun MovieDetailScreenPreview() {
    MaterialTheme {
        MovieDetailScreen(
            state = MovieDetailUiState(
                isLoading = false,
                detail = MovieDetailPreviewParameterProvider().values.first(),
                cast = CastPreviewParameterProvider().values.first(),
                recommendations = MoviePreviewParameterProvider().values.first(),
            ),
            onAction = {},
            onRecommendationClick = { _, _ -> },
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun MovieDetailScreenFavoritePreview() {
    MaterialTheme {
        MovieDetailScreen(
            state = MovieDetailUiState(
                isLoading = false,
                detail = MovieDetailPreviewParameterProvider().values.first(),
                cast = CastPreviewParameterProvider().values.first(),
                recommendations = MoviePreviewParameterProvider().values.first(),
                isFavorite = true,
            ),
            onAction = {},
            onRecommendationClick = { _, _ -> },
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun MovieDetailScreenLoadingPreview() {
    MaterialTheme {
        MovieDetailScreen(
            state = MovieDetailUiState(),
            onAction = {},
            onRecommendationClick = { _, _ -> },
        )
    }
}
