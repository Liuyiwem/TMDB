package com.yiwenliu.feature.home.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.yiwenliu.core.common.presentation.util.toUserMessage
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.ui.ErrorItem
import com.yiwenliu.core.ui.MovieCategoryTab
import com.yiwenliu.core.ui.MovieCategoryTabRow
import com.yiwenliu.core.ui.MoviePagingGrid
import com.yiwenliu.core.ui.MoviePreviewParameterProvider
import kotlinx.coroutines.flow.flowOf

@Composable
internal fun HomeRoot(
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
    val isRefreshing by remember {
        derivedStateOf { movies.loadState.refresh is LoadState.Loading }
    }

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
            isRefreshing = isRefreshing,
            onRefresh = { movies.refresh() },
            modifier = Modifier.fillMaxSize(),
        ) {
            when (val refresh = movies.loadState.refresh) {
                is LoadState.Error -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    ErrorItem(
                        errorMessage = refresh.error.toUserMessage(LocalContext.current),
                        retryText = stringResource(com.yiwenliu.core.ui.R.string.retry),
                        onRetry = movies::retry,
                    )
                }

                is LoadState.NotLoading -> MoviePagingGrid(movies = movies, testTagPrefix = "home")

                else -> {}
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
        )
    }
}
