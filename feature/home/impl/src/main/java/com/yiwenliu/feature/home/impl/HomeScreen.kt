package com.yiwenliu.feature.home.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.yiwenliu.core.common.domain.util.NetworkException
import com.yiwenliu.core.common.presentation.util.toString
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.ui.ErrorItem
import com.yiwenliu.core.ui.MovieCategoryTabs
import com.yiwenliu.core.ui.MovieItem
import com.yiwenliu.core.ui.MoviePreviewParameterProvider
import kotlinx.coroutines.flow.flowOf

@Composable
fun HomeRoot(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val movies = viewModel.moviesPager.collectAsLazyPagingItems()

    HomeScreen(
        state = state,
        movies = movies,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@Composable
internal fun HomeScreen(
    state: HomeUiState,
    movies: LazyPagingItems<Movie>,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isRefreshing by remember(movies) {
        derivedStateOf { movies.loadState.refresh is LoadState.Loading }
    }

    Column(modifier = modifier.fillMaxSize()) {
        MovieCategoryTabs(
            categories = state.categories,
            titles = state.categories.map { stringResource(it.titleRes()) },
            selectedCategory = state.selectedCategory,
            onCategorySelected = { onAction(HomeAction.OnCategorySelected(it)) },
        )
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { movies.refresh() },
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("home:grid"),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
            ) {
                when (movies.loadState.refresh) {
                    is LoadState.Error -> {
                        val error = (movies.loadState.refresh as LoadState.Error).error
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ErrorItem(
                                errorMessage = errorMessageFor(error),
                                retryText = stringResource(R.string.retry),
                                onRetry = { movies.retry() },
                            )
                        }
                    }
                    else -> {}
                }

                items(
                    count = movies.itemCount,
                    key = movies.itemKey { it.id },
                ) { index ->
                    movies[index]?.let { movie ->
                        MovieItem(movie = movie, modifier = Modifier.fillMaxWidth())
                    }
                }

                when (movies.loadState.append) {
                    is LoadState.Loading -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("home:appendLoading"),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    is LoadState.Error -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            val error = (movies.loadState.append as LoadState.Error).error
                            ErrorItem(
                                errorMessage = errorMessageFor(error),
                                retryText = stringResource(R.string.retry),
                                onRetry = { movies.retry() },
                            )
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun errorMessageFor(error: Throwable): String {
    val context = LocalContext.current
    return if (error is NetworkException) {
        error.networkError.toString(context)
    } else {
        stringResource(R.string.unknown_error)
    }
}

@StringRes
private fun MovieCategory.titleRes(): Int =
    when (this) {
        MovieCategory.NOW_PLAYING -> R.string.tab_now_playing
        MovieCategory.POPULAR -> R.string.tab_popular
        MovieCategory.TOP_RATED -> R.string.tab_top_rated
        MovieCategory.UPCOMING -> R.string.tab_upcoming
    }

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun HomeScreenPreview() {
    val sampleMovies = MoviePreviewParameterProvider().values.first()
    val successAndEnded =
        LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = true),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true),
        )
    val pagingItems =
        flowOf(
            PagingData.from(
                sampleMovies,
                sourceLoadStates = successAndEnded,
            ),
        ).collectAsLazyPagingItems()
    MaterialTheme {
        Scaffold { innerPadding ->
            HomeScreen(
                state = HomeUiState(),
                movies = pagingItems,
                onAction = {},
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun HomeScreenRefreshingPreview() {
    val sampleMovies = MoviePreviewParameterProvider().values.first()
    val refreshing =
        LoadStates(
            refresh = LoadState.Loading,
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true),
        )
    val pagingItems =
        flowOf(PagingData.from(sampleMovies, sourceLoadStates = refreshing))
            .collectAsLazyPagingItems()
    MaterialTheme {
        HomeScreen(
            state = HomeUiState(),
            movies = pagingItems,
            onAction = {},
        )
    }
}
