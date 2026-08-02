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
        val appendError = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = false),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.Error(NetworkException(NetworkError.NO_INTERNET)),
        )
        composeTestRule.setContent {
            val items = flowOf(
                PagingData.from(sampleMovies.take(2), sourceLoadStates = appendError),
            ).collectAsLazyPagingItems()
            MaterialTheme {
                HomeScreen(state = HomeUiState(), movies = items, onAction = {})
            }
        }
        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(com.yiwenliu.core.common.R.string.error_no_internet),
            ).assertIsDisplayed()
        composeTestRule.onNodeWithText(sampleMovies.first().title).assertIsDisplayed()
    }

    @Test
    fun homeScreen_appendErrorRetry_clearsTheError() {
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
        composeTestRule.waitUntil(TIMEOUT_MILLIS) {
            composeTestRule.onAllNodesWithTag("error").fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun homeScreen_refreshLoadingWithStaleItems_doesNotRenderGrid() {
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
        val SETTLED = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = true),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true),
        )

        const val TIMEOUT_MILLIS = 5_000L
    }
}

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
