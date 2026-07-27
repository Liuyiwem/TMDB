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
    )
}

@Composable
internal fun SearchScreen(
    state: SearchUiState,
    searchMovies: LazyPagingItems<Movie>,
    onAction: (SearchAction) -> Unit,
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
            // 不是無條件 true：切換分頁會讓搜尋頁的組合被丟棄，回來時 LaunchedEffect 會重跑。
            // 無條件搶焦點就會變成「回到搜尋頁時鍵盤蓋住你正在看的結果」。
            autoFocus = state.queryString.isEmpty(),
        )
        SearchResults(
            searchMovies = searchMovies,
            isIdle = state.isIdle,
            isPending = state.isPending,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun SearchResults(
    searchMovies: LazyPagingItems<Movie>,
    isIdle: Boolean,
    isPending: Boolean,
    modifier: Modifier = Modifier,
) {
    // derivedStateOf 是必要的而非裝飾：loadState 與 itemCount 都是 mutableStateOf 支撐，直接讀
    // 會讓 SearchResults 在每一次 load state 轉換與每一次 itemCount 成長時都重組（捲動時每載入
    // 一頁就好幾次）。包起來之後只有 5 值的 resultsState 真的改變才重組。
    //
    // key 只列 isIdle / isPending：它們是會變的參數，被 lambda 捕獲後會凍結在建立時的值。
    // searchMovies 不需要當 key —— collectAsLazyPagingItems 內部是 remember(flow)，而
    // viewModel.searchMoviePager 是 val，參考在整個組合生命週期內不會變。
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

    // Box + contentAlignment 而不是 Column：原本 spinner 是 grid 的兄弟節點，而
    // .align(Alignment.CenterHorizontally) 在 ColumnScope 只管水平對齊，所以 spinner 會卡在
    // 頂端並把下方 fillMaxSize 的 grid 往下推、在每次載入開始／結束時重新量測。
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // 綁定 when 的 subject：resultsState 是委派屬性，不綁定的話 Kotlin 無法 smart cast，
        // 就得在分支裡二次讀取再硬轉型。
        when (val current = resultsState) {
            // 還沒輸入：刻意留白，不顯示提示語也不顯示空的 grid。
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

/** 五個 preview 共用的本體，原本每個 preview 各自重複一份 LoadStates 與 PagingData 建構。 */
@Composable
private fun SearchScreenPreview(
    state: SearchUiState,
    movies: List<Movie>,
    loadStates: LoadStates = SETTLED_LOAD_STATES,
) {
    val pagingItems = flowOf(PagingData.from(movies, sourceLoadStates = loadStates))
        .collectAsLazyPagingItems()
    MaterialTheme {
        SearchScreen(state = state, searchMovies = pagingItems, onAction = {})
    }
}

private val SETTLED_LOAD_STATES = LoadStates(
    refresh = LoadState.NotLoading(endOfPaginationReached = true),
    prepend = LoadState.NotLoading(endOfPaginationReached = true),
    append = LoadState.NotLoading(endOfPaginationReached = true),
)
