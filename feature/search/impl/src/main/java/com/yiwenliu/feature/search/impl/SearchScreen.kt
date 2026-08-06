package com.yiwenliu.feature.search.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.yiwenliu.core.common.presentation.util.toUserMessage
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.ui.ErrorItem
import com.yiwenliu.core.ui.MoviePagingGrid
import com.yiwenliu.core.ui.MoviePreviewParameterProvider
import com.yiwenliu.core.ui.SearchTextField
import kotlinx.coroutines.flow.flowOf

@Composable
internal fun SearchRoot(
    onMovieClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val searchMovies = viewModel.searchMoviePager.collectAsLazyPagingItems()

    SearchScreen(
        state = state,
        searchMovies = searchMovies,
        modifier = modifier,
        onAction = viewModel::onAction,
        onMovieClick = onMovieClick,
    )
}

@Composable
internal fun SearchScreen(
    state: SearchUiState,
    searchMovies: LazyPagingItems<Movie>,
    onAction: (SearchAction) -> Unit,
    onMovieClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        SearchTextField(
            queryString = state.queryString,
            searchIconContentDescription = stringResource(R.string.search),
            closeIconContentDescription = stringResource(R.string.close),
            onQueryStringChanged = { queryString ->
                onAction(SearchAction.OnQueryStringChanged(queryString))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("search:textField"),
            autoFocus = state.queryString.isEmpty(),
        )
        SearchResults(
            searchMovies = searchMovies,
            isIdle = state.isIdle,
            isPending = state.isPending,
            onMovieClick = onMovieClick,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun SearchResults(
    searchMovies: LazyPagingItems<Movie>,
    isIdle: Boolean,
    isPending: Boolean,
    onMovieClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val resultsState by remember(isIdle, isPending) {
        derivedStateOf {
            searchResultsStateOf(
                isIdle = isIdle,
                isPending = isPending,
                refresh = searchMovies.loadState.refresh,
                itemCount = searchMovies.itemCount,
            )
        }
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when (val current = resultsState) {
            SearchResultsState.Idle -> Unit

            SearchResultsState.Loading -> CircularProgressIndicator(
                modifier = Modifier.testTag("search:loading"),
            )

            is SearchResultsState.Error -> ErrorItem(
                errorMessage = current.throwable.toUserMessage(LocalContext.current),
                retryText = stringResource(com.yiwenliu.core.ui.R.string.retry),
                onRetry = searchMovies::retry,
            )

            SearchResultsState.Empty -> Text(
                text = stringResource(R.string.search_no_results),
                modifier = Modifier
                    .padding(16.dp)
                    .testTag("search:empty"),
            )

            SearchResultsState.Results -> MoviePagingGrid(
                movies = searchMovies,
                onMovieClick = onMovieClick,
                testTagPrefix = "search",
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun SearchScreenIdlePreview() {
    SearchScreenPreview(state = SearchUiState(queryString = ""), movies = emptyList())
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun SearchScreenResultsPreview() {
    SearchScreenPreview(
        state = SearchUiState(queryString = "Search"),
        movies = MoviePreviewParameterProvider().values.first(),
    )
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun SearchScreenPendingPreview() {
    SearchScreenPreview(
        state = SearchUiState(queryString = "Sea", isPending = true),
        movies = emptyList(),
    )
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun SearchScreenRefreshingPreview() {
    SearchScreenPreview(
        state = SearchUiState(queryString = "Search"),
        movies = MoviePreviewParameterProvider().values.first(),
        loadStates = SETTLED_LOAD_STATES.copy(refresh = LoadState.Loading),
    )
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun SearchScreenEmptyPreview() {
    SearchScreenPreview(state = SearchUiState(queryString = "qxzqxz"), movies = emptyList())
}

@Composable
private fun SearchScreenPreview(
    state: SearchUiState,
    movies: List<Movie>,
    loadStates: LoadStates = SETTLED_LOAD_STATES,
) {
    val pagingItems = flowOf(PagingData.from(movies, sourceLoadStates = loadStates))
        .collectAsLazyPagingItems()
    MaterialTheme {
        SearchScreen(
            state = state,
            searchMovies = pagingItems,
            onAction = {},
            onMovieClick = { _, _ -> },
        )
    }
}

private val SETTLED_LOAD_STATES = LoadStates(
    refresh = LoadState.NotLoading(endOfPaginationReached = true),
    prepend = LoadState.NotLoading(endOfPaginationReached = true),
    append = LoadState.NotLoading(endOfPaginationReached = true),
)
