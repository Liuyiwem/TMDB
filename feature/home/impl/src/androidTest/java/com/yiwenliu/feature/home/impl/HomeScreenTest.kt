package com.yiwenliu.feature.home.impl

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.compose.collectAsLazyPagingItems
import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.NetworkException
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.ui.ErrorItem
import com.yiwenliu.core.ui.MoviePreviewParameterProvider
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HomeScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val sampleMovies: List<Movie> = MoviePreviewParameterProvider().values.first()

    @Test
    fun homeScreen_showsTabsAndMovies() {
        composeTestRule.setContent {
            val items = flowOf(PagingData.from(sampleMovies, sourceLoadStates = SETTLED))
                .collectAsLazyPagingItems()
            MaterialTheme {
                HomeScreen(
                    state = HomeUiState(selectedCategory = MovieCategory.NOW_PLAYING),
                    movies = items,
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("tabRow").assertIsDisplayed()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.tab_now_playing))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(sampleMovies.first().title).assertIsDisplayed()
    }

    @Test
    fun homeScreen_tabClick_invokesOnActionWithCategory() {
        var lastAction: HomeAction? = null
        composeTestRule.setContent {
            val items = flowOf(PagingData.from(sampleMovies, sourceLoadStates = SETTLED))
                .collectAsLazyPagingItems()
            MaterialTheme {
                HomeScreen(
                    state = HomeUiState(selectedCategory = MovieCategory.NOW_PLAYING),
                    movies = items,
                    onAction = { lastAction = it },
                )
            }
        }

        composeTestRule.onNodeWithTag("tab:TOP_RATED").performClick()
        assertEquals(HomeAction.OnCategorySelected(MovieCategory.TOP_RATED), lastAction)
    }

    @Test
    fun homeScreen_errorRefresh_showsRetry() {
        val errorStates =
            LoadStates(
                refresh = LoadState.Error(RuntimeException("boom")),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            )
        composeTestRule.setContent {
            val items =
                flowOf(
                    PagingData.from(emptyList<Movie>(), sourceLoadStates = errorStates),
                ).collectAsLazyPagingItems()
            MaterialTheme {
                HomeScreen(
                    state = HomeUiState(),
                    movies = items,
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("error").assertIsDisplayed()
        // retry 已搬到 core:ui（MoviePagingGrid 與各 feature 共用），而 nonTransitiveRClass
        // 讓 feature 的 R 不再包含它，所以必須指名 core.ui.R。
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(com.yiwenliu.core.ui.R.string.retry))
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_appendLoading_showsIndicator() {
        val appendLoading =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = false),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.Loading,
            )
        composeTestRule.setContent {
            val items =
                flowOf(
                    PagingData.from(sampleMovies, sourceLoadStates = appendLoading),
                ).collectAsLazyPagingItems()
            MaterialTheme {
                HomeScreen(
                    state = HomeUiState(),
                    movies = items,
                    onAction = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag("home:grid")
            .performScrollToNode(hasTestTag("home:appendLoading"))
        composeTestRule.onNodeWithTag("home:appendLoading").assertIsDisplayed()
    }

    @Test
    fun homeScreen_appendError_showsMappedMessageAndKeepsLoadedItems() {
        // 這一半【不】需要真的 Pager。PagingData.from 的 sourceLoadStates 照單全收，
        // append 的 LoadState.Error 產得出來（同一個檔案的 appendLoading 與 errorRefresh
        // 測試已經在用同一招）。被寫死成 no-op 的只有 retry()——那是下一個測試的事。
        val appendError = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = false),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.Error(NetworkException(NetworkError.NO_INTERNET)),
        )
        composeTestRule.setContent {
            // 只放兩筆，錯誤項目才會落在第一個視窗內、不需要捲動
            // （對照 homeScreen_appendLoading_showsIndicator 用了 performScrollToNode）。
            val items = flowOf(
                PagingData.from(sampleMovies.take(2), sourceLoadStates = appendError),
            ).collectAsLazyPagingItems()
            MaterialTheme {
                HomeScreen(state = HomeUiState(), movies = items, onAction = {})
            }
        }

        // 驗 NetworkError -> 使用者訊息真的接到畫面上。NetworkErrorToStringTest 驗的是
        // 映射表【本身】，這裡驗的是 MoviePagingGrid 有呼叫它、而且結果顯示出來了。
        // nonTransitiveRClass 讓 core:common 的 R 必須指名。
        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(com.yiwenliu.core.common.R.string.error_no_internet),
            ).assertIsDisplayed()

        // 第一頁的電影必須還在 —— append 失敗是網格【內】的一個項目，不該取代整個清單。
        // 這正是 MoviePagingGrid 只負責 append、把 refresh 交給呼叫端的那條分工。
        composeTestRule.onNodeWithText(sampleMovies.first().title).assertIsDisplayed()
    }

    @Test
    fun homeScreen_appendErrorRetry_clearsTheError() {
        // 這一項【必須】用真的 Pager：PagingData.from 建構時傳的是
        // uiReceiver = NOOP_UI_RECEIVER，所以 LazyPagingItems.retry() -> presenter.retry()
        // -> uiReceiver.retry() 是一個空實作。「按下重試真的會重抓」只有在真的
        // PagingSource 後面才驗得到。
        val pagerFlow = Pager(PagingConfig(pageSize = 2, enablePlaceholders = false)) {
            AppendFailsOncePagingSource(sampleMovies.take(2))
        }.flow

        composeTestRule.setContent {
            val items = pagerFlow.collectAsLazyPagingItems()
            MaterialTheme {
                HomeScreen(state = HomeUiState(), movies = items, onAction = {})
            }
        }

        composeTestRule.waitUntil(TIMEOUT_MILLIS) {
            composeTestRule.onAllNodesWithTag("error").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("retry").performClick()

        // 斷言【使用者看得到的結果】，而不是「PagingSource 被打了幾次」——
        // 計數器可以照樣增加而畫面仍然壞著。錯誤項目消失才同時代表 retry 打回了 source、
        // source 成功了、而且 UI 跟上了。PagingData.from 版本在這裡會永遠超時。
        composeTestRule.waitUntil(TIMEOUT_MILLIS) {
            composeTestRule.onAllNodesWithTag("error").fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun homeScreen_refreshLoadingWithStaleItems_doesNotRenderGrid() {
        // 使用者實測回報的回歸：有資料 -> 切斷網路 -> 下拉刷新 -> 出現 retry ->
        // 按下 retry -> 閃過先前資料。
        //
        // 不去追那個「閃一下」本身 —— 那是時序問題，第一版用 mainClock 逐 frame 檢查，
        // 結果是空轉的測試：凍住的是 Compose 的 frame clock，而 Paging 的載入跑在協程
        // 排程器上，30 個 frame 內狀態根本沒變成 Loading，斷言就無意義地通過了
        // （把生產碼改回舊寫法仍然綠）。
        //
        // 改成直接測【造成閃爍的那個不變量】：refresh 正在載入而上一份清單還在時，
        // grid 不該被渲染。這正是 retry 那一瞬間的狀態，而且完全不需要玩時鐘。
        val refreshingWithStaleItems = LoadStates(
            refresh = LoadState.Loading,
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true),
        )
        composeTestRule.setContent {
            val items = flowOf(
                PagingData.from(sampleMovies, sourceLoadStates = refreshingWithStaleItems),
            ).collectAsLazyPagingItems()
            MaterialTheme {
                HomeScreen(state = HomeUiState(), movies = items, onAction = {})
            }
        }

        composeTestRule.onNodeWithTag("home:grid").assertDoesNotExist()
        composeTestRule.onNodeWithText(sampleMovies.first().title).assertDoesNotExist()
    }

    @Test
    fun errorItem_retryClick_invokesCallback() {
        var retried = false
        composeTestRule.setContent {
            MaterialTheme {
                ErrorItem(
                    errorMessage = "boom",
                    retryText = "Retry",
                    onRetry = { retried = true },
                )
            }
        }

        composeTestRule.onNodeWithTag("retry").performClick()
        assertTrue(retried)
    }

    private companion object {
        /**
         * 「refresh 已完成、沒有更多頁」。
         *
         * 沒有這個參數的 `PagingData.from(list)` 不會派發任何 load state，`refresh` 會停在
         * paging-compose 的初始值 `LoadState.Loading` —— 也就是「永遠在載入卻已經有資料」
         * 這個真實 Pager 不可能產生的狀態。舊的 `else -> grid` 把它吞掉了，所以這兩個測試
         * 一直是綠的；改成只有 NotLoading 渲染 grid 之後才露出來。
         */
        val SETTLED = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = true),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true),
        )

        const val TIMEOUT_MILLIS = 5_000L
    }
}

/**
 * 第一頁成功、第一次 append 失敗、之後的 append 成功。
 *
 * 計數器是私有的:測試不讀它，而是斷言畫面上的錯誤項目有沒有消失。
 * retry() 重試的是【同一個】source 實例（refresh 才會經由 factory 建新的），所以計數會延續；
 * 而 Paging 對同一個 source 的載入是序列化的，只有 load() 碰這個欄位，
 * 因此不需要 AtomicInteger。
 */
private class AppendFailsOncePagingSource(
    private val firstPage: List<Movie>,
) : PagingSource<Int, Movie>() {
    private var appendAttempts = 0

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: 1
        if (page == 1) return LoadResult.Page(firstPage, prevKey = null, nextKey = 2)
        return if (appendAttempts++ == 0) {
            LoadResult.Error(NetworkException(NetworkError.NO_INTERNET))
        } else {
            LoadResult.Page(emptyList(), prevKey = 1, nextKey = null)
        }
    }
}
