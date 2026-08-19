package com.yiwenliu.feature.home.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.ui.LocalSnackbarHostState
import com.yiwenliu.core.ui.component.ErrorItem
import com.yiwenliu.core.ui.component.MovieCategoryTab
import com.yiwenliu.core.ui.component.MovieCategoryTabRow
import com.yiwenliu.core.ui.component.MoviePagingGrid
import com.yiwenliu.core.ui.preview.MoviePreviewParameterProvider
import com.yiwenliu.core.ui.util.toUiText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf

@Composable
internal fun HomeRoot(
    onMovieClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val movies = key(state.selectedCategory) { viewModel.moviesPager.collectAsLazyPagingItems() }

    RefreshErrorSnackbarEffect(movies)

    HomeScreen(
        state = state,
        movies = movies,
        onAction = viewModel::onAction,
        onMovieClick = onMovieClick,
        modifier = modifier,
    )
}

@Composable
internal fun RefreshErrorSnackbarEffect(movies: LazyPagingItems<Movie>) {
    val snackbarHostState = LocalSnackbarHostState.current
    val context = LocalContext.current
    val retryLabel = stringResource(com.yiwenliu.core.ui.R.string.retry)

    LaunchedEffect(movies, snackbarHostState) {
        snapshotFlow {
            val refresh = movies.loadState.refresh
            if (refresh is LoadState.Error && movies.itemCount > 0) refresh.error else null
        }
            .distinctUntilChanged()
            .collectLatest { error ->
                if (error == null) return@collectLatest
                val result =
                    snackbarHostState.showSnackbar(
                        message = error.toUiText().asString(context),
                        actionLabel = retryLabel,
                        duration = SnackbarDuration.Long,
                    )
                if (result == SnackbarResult.ActionPerformed) movies.retry()
            }
    }
}

@Composable
internal fun HomeScreen(
    state: HomeUiState,
    movies: LazyPagingItems<Movie>,
    onAction: (HomeAction) -> Unit,
    onMovieClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val refresh = movies.loadState.refresh
    val hasItems = movies.itemCount > 0

    Column(modifier = modifier.fillMaxSize()) {
        MovieCategoryTabRow(
            selectedTabIndex = MovieCategory.entries.indexOf(state.selectedCategory).coerceAtLeast(0),
        ) {
            MovieCategory.entries.forEach { category ->
                MovieCategoryTab(
                    category = category,
                    selected = category == state.selectedCategory,
                    onClick = { onAction(HomeAction.OnCategorySelected(category)) },
                    text = { Text(stringResource(category.titleRes())) },
                )
            }
        }
        PullToRefreshBox(
            isRefreshing = hasItems && refresh is LoadState.Loading,
            onRefresh = movies::refresh,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when {
                hasItems -> MoviePagingGrid(
                    movies = movies,
                    onMovieClick = onMovieClick,
                    testTagPrefix = HomeTestTags.PREFIX,
                )

                refresh is LoadState.Error -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    ErrorItem(
                        errorMessage = refresh.error.toUiText().asString(),
                        retryText = stringResource(com.yiwenliu.core.ui.R.string.retry),
                        onRetry = movies::retry,
                    )
                }

                refresh is LoadState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.testTag(HomeTestTags.LOADING))
                }
            }
        }
    }
}

@StringRes
private fun MovieCategory.titleRes(): Int = when (this) {
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
        HomeScreen(
            state = HomeUiState(),
            movies = pagingItems,
            onAction = {},
            onMovieClick = { _, _ -> },
        )
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
            onMovieClick = { _, _ -> },
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun HomeScreenLoadingPreview() {
    val loading =
        LoadStates(
            refresh = LoadState.Loading,
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true),
        )
    val pagingItems =
        flowOf(PagingData.from(emptyList<Movie>(), sourceLoadStates = loading))
            .collectAsLazyPagingItems()
    MaterialTheme {
        HomeScreen(
            state = HomeUiState(),
            movies = pagingItems,
            onAction = {},
            onMovieClick = { _, _ -> },
        )
    }
}
