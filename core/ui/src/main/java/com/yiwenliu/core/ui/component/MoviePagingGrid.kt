package com.yiwenliu.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.ui.R
import com.yiwenliu.core.ui.TmdbTestTags
import com.yiwenliu.core.ui.preview.MoviePreviewParameterProvider
import com.yiwenliu.core.ui.util.toUiText
import kotlinx.coroutines.flow.flowOf

@Composable
fun MoviePagingGrid(
    movies: LazyPagingItems<Movie>,
    onMovieClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = TmdbTestTags.DEFAULT_GRID_PREFIX,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag(TmdbTestTags.grid(testTagPrefix)),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        items(
            count = movies.itemCount,
            key = movies.itemKey { it.id },
        ) { index ->
            movies[index]?.let { movie ->
                MovieItem(
                    movie = movie,
                    onClick = { onMovieClick(movie.id, movie.title) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        when (val append = movies.loadState.append) {
            is LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag(TmdbTestTags.appendLoading(testTagPrefix)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                ErrorItem(
                    errorMessage = append.error.toUiText().asString(),
                    retryText = stringResource(R.string.retry),
                    onRetry = movies::retry,
                )
            }

            else -> Unit
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun MoviePagingGridPreview() {
    val movies = flowOf(PagingData.from(MoviePreviewParameterProvider().values.first()))
        .collectAsLazyPagingItems()
    MaterialTheme {
        MoviePagingGrid(movies = movies, onMovieClick = { _, _ -> })
    }
}
