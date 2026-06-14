package com.yiwenliu.feature.movie

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.yiwenliu.core.common.domain.util.NetworkException
import com.yiwenliu.core.common.presentation.util.toString
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.ui.MovieItem
import com.yiwenliu.core.ui.MoviePreviewParameterProvider
import kotlinx.coroutines.flow.flowOf

@Composable
fun MovieRoute(
    modifier: Modifier = Modifier,
    viewModel: MovieViewModel = hiltViewModel(),
) {
    val popularMovies = viewModel.popularMoviesPager.collectAsLazyPagingItems()
    val isRefreshing by remember(popularMovies) {
        derivedStateOf { popularMovies.loadState.refresh is LoadState.Loading }
    }

    MovieScreen(
        popularMovies = popularMovies,
        isRefreshing = isRefreshing,
        onRefresh = { popularMovies.refresh() },
        modifier = modifier,
    )
}

@Composable
internal fun MovieScreen(
    popularMovies: LazyPagingItems<Movie>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 120.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("movie:grid"),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(R.string.popular_movies),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            when (popularMovies.loadState.refresh) {
                is LoadState.Error -> {
                    val error = (popularMovies.loadState.refresh as LoadState.Error).error
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ErrorItem(
                            error = error,
                            onRetry = { popularMovies.retry() },
                        )
                    }
                }
                else -> {}
            }

            items(
                count = popularMovies.itemCount,
                key = popularMovies.itemKey { it.id },
            ) { index ->
                popularMovies[index]?.let { movie ->
                    MovieItem(movie = movie, modifier = Modifier.fillMaxWidth())
                }
            }

            when (popularMovies.loadState.append) {
                is LoadState.Loading -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("movie:appendLoading"),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                is LoadState.Error -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        val error = (popularMovies.loadState.append as LoadState.Error).error
                        ErrorItem(
                            error = error,
                            onRetry = { popularMovies.retry() },
                        )
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
internal fun ErrorItem(
    error: Throwable,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("movie:error"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val context = LocalContext.current
        val message =
            if (error is NetworkException) {
                error.networkError.toString(context)
            } else {
                stringResource(R.string.unknown_error)
            }
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry, modifier = Modifier.testTag("movie:retry")) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun MovieScreenPreview() {
    val sampleMovies = MoviePreviewParameterProvider().values.first()
    val pagingItems = flowOf(PagingData.from(sampleMovies)).collectAsLazyPagingItems()
    MaterialTheme {
        Scaffold { innerPadding ->
            MovieScreen(
                popularMovies = pagingItems,
                isRefreshing = false,
                onRefresh = {},
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorItemPreview() {
    MaterialTheme {
        ErrorItem(
            error = Exception("Network Error"),
            onRetry = {},
        )
    }
}
