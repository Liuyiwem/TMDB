package com.yiwenliu.feature.search.impl

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.NetworkException
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.ui.MoviePreviewParameterProvider
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class SearchScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // androidTest 的 classpath 上沒有 core:testing，所以用 core:ui 的 preview fixture ——
    // 這也是 HomeScreenTest 的做法。
    private val sampleMovies: List<Movie> = MoviePreviewParameterProvider().values.first()

    private val settled = LoadStates(
        refresh = LoadState.NotLoading(endOfPaginationReached = true),
        prepend = LoadState.NotLoading(endOfPaginationReached = true),
        append = LoadState.NotLoading(endOfPaginationReached = true),
    )

    /**
     * 用 [PagingData.from] 的 `sourceLoadStates` 直接指定 load state，不需要真的跑 Pager
     * 或網路。這是 HomeScreenTest 已經在用的技巧。
     */
    @Composable
    private fun Screen(
        state: SearchUiState,
        movies: List<Movie> = emptyList(),
        loadStates: LoadStates = settled,
        onAction: (SearchAction) -> Unit = {},
    ) {
        val items = flowOf(PagingData.from(movies, sourceLoadStates = loadStates))
            .collectAsLazyPagingItems()
        MaterialTheme {
            SearchScreen(state = state, searchMovies = items, onAction = onAction)
        }
    }

    @Test
    fun blankQuery_showsNeitherEmptyNorLoading() {
        // SearchViewModel 的空白分支送出 PagingData.empty(sourceLoadStates = ...)。若有人把那個
        // 參數簡化掉，paging-compose 的初始 refresh 會停在 LoadState.Loading，空白搜尋頁就變成
        // 永久轉圈 —— 這個測試會抓到。
        composeTestRule.setContent { Screen(state = SearchUiState(queryString = "")) }

        composeTestRule.onNodeWithTag("search:empty").assertDoesNotExist()
        composeTestRule.onNodeWithTag("search:loading").assertDoesNotExist()
        composeTestRule.onNodeWithTag("search:grid").assertDoesNotExist()
    }

    @Test
    fun pendingQuery_showsLoadingNotEmpty() {
        // 「打第一個字先閃一下 No results found」在 UI 層的回歸測試。
        // queryString 非空白、pager 還停在 NotLoading + 0 筆 —— 修正前這裡會顯示 search:empty。
        composeTestRule.setContent {
            Screen(state = SearchUiState(queryString = "b", isPending = true))
        }

        composeTestRule.onNodeWithTag("search:loading").assertIsDisplayed()
        composeTestRule.onNodeWithTag("search:empty").assertDoesNotExist()
    }

    @Test
    fun refreshLoading_showsLoadingIndicator() {
        composeTestRule.setContent {
            Screen(
                state = SearchUiState(queryString = "batman"),
                loadStates = settled.copy(refresh = LoadState.Loading),
            )
        }

        composeTestRule.onNodeWithTag("search:loading").assertIsDisplayed()
    }

    @Test
    fun settledQueryWithNoResults_showsEmptyMessage() {
        composeTestRule.setContent { Screen(state = SearchUiState(queryString = "qxzqxz")) }

        composeTestRule.onNodeWithTag("search:empty").assertIsDisplayed()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.search_no_results))
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("search:grid").assertDoesNotExist()
    }

    @Test
    fun resultsPresent_showsGridWithFirstTitle() {
        composeTestRule.setContent {
            Screen(state = SearchUiState(queryString = "batman"), movies = sampleMovies)
        }

        composeTestRule.onNodeWithTag("search:grid").assertIsDisplayed()
        composeTestRule.onNodeWithText(sampleMovies.first().title).assertIsDisplayed()
    }

    @Test
    fun refreshError_showsMappedMessageAndRetry() {
        composeTestRule.setContent {
            Screen(
                state = SearchUiState(queryString = "batman"),
                // 餵 NetworkException 而不是裸的 IOException：MoviePagingSource 一律把錯誤
                // 包成 NetworkException，裸的 IOException 在生產環境到不了 UI
                // （它會走 toUserMessage 的 else 分支顯示「Unknown error」，
                // 那不是任何真實情境）。
                loadStates = settled.copy(
                    refresh = LoadState.Error(NetworkException(NetworkError.NO_INTERNET)),
                ),
            )
        }

        // 斷言【映射後的訊息】而不只是 tag。ErrorItem 對任何錯誤都貼 error/retry 這兩個
        // tag，那是常數——只驗 tag 的話，NetworkError -> 字串的接線壞掉是看不出來的。
        // nonTransitiveRClass 讓 core:common 的 R 必須指名。
        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(com.yiwenliu.core.common.R.string.error_no_internet),
            ).assertIsDisplayed()
        composeTestRule.onNodeWithTag("retry").assertIsDisplayed()
        // 錯誤要贏過「0 筆 → 找不到結果」。
        composeTestRule.onNodeWithTag("search:empty").assertDoesNotExist()
    }

    @Test
    fun appendLoading_showsIndicatorAtEndOfGrid() {
        composeTestRule.setContent {
            Screen(
                state = SearchUiState(queryString = "batman"),
                movies = sampleMovies,
                loadStates = settled.copy(append = LoadState.Loading),
            )
        }

        composeTestRule.onNodeWithTag("search:grid")
            .performScrollToNode(hasTestTag("search:appendLoading"))
        composeTestRule.onNodeWithTag("search:appendLoading").assertIsDisplayed()
    }

    @Test
    fun typing_emitsOnQueryStringChanged() {
        var lastAction: SearchAction? = null
        composeTestRule.setContent {
            Screen(state = SearchUiState(queryString = ""), onAction = { lastAction = it })
        }

        composeTestRule.onNodeWithTag("search:textField").performTextInput("batman")

        assertEquals(SearchAction.OnQueryStringChanged("batman"), lastAction)
    }

    @Test
    fun pastingTextWithNewlines_collapsesThemIntoSpaces() {
        // SearchTextField 換行修正的回歸測試。修正前含換行的輸入會【整筆】被丟棄，
        // lastAction 會是 null。
        var lastAction: SearchAction? = null
        composeTestRule.setContent {
            Screen(state = SearchUiState(queryString = ""), onAction = { lastAction = it })
        }

        composeTestRule.onNodeWithTag("search:textField").performTextInput("Fight\nClub")

        assertEquals(SearchAction.OnQueryStringChanged("Fight Club"), lastAction)
    }

    @Test
    fun clickingClearIcon_emitsEmptyQuery() {
        var lastAction: SearchAction? = null
        composeTestRule.setContent {
            Screen(state = SearchUiState(queryString = "batman"), onAction = { lastAction = it })
        }

        // 清除鍵沒有 tag，只有 contentDescription。
        composeTestRule
            .onNodeWithContentDescription(composeTestRule.activity.getString(R.string.close))
            .performClick()

        assertEquals(SearchAction.OnQueryStringChanged(""), lastAction)
    }
}
