package com.yiwenliu.feature.home.impl

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.yiwenliu.core.common.domain.util.DataError
import com.yiwenliu.core.common.domain.util.DataErrorException
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.ui.LocalSnackbarHostState
import com.yiwenliu.core.ui.TmdbTestTags
import com.yiwenliu.core.ui.component.ErrorItem
import com.yiwenliu.core.ui.preview.MoviePreviewParameterProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HomeScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val sampleMovies: List<Movie> = MoviePreviewParameterProvider().values.first()

    private val noInternetMessage: String
        get() = composeTestRule.activity.getString(com.yiwenliu.core.ui.R.string.error_no_internet)

    private val retryLabel: String
        get() = composeTestRule.activity.getString(com.yiwenliu.core.ui.R.string.retry)

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
                    onMovieClick = { _, _ -> },
                )
            }
        }
        composeTestRule.onNodeWithTag(TmdbTestTags.TAB_ROW).assertIsDisplayed()
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
                    onMovieClick = { _, _ -> },
                )
            }
        }
        composeTestRule.onNodeWithTag(TmdbTestTags.tab(MovieCategory.TOP_RATED)).performClick()
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
                    onMovieClick = { _, _ -> },
                )
            }
        }
        composeTestRule.onNodeWithTag(TmdbTestTags.ERROR).assertIsDisplayed()
        composeTestRule.onNodeWithText(retryLabel).assertIsDisplayed()
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
                    onMovieClick = { _, _ -> },
                )
            }
        }
        composeTestRule
            .onNodeWithTag(HomeTestTags.GRID)
            .performScrollToNode(hasTestTag(HomeTestTags.APPEND_LOADING))
        composeTestRule.onNodeWithTag(HomeTestTags.APPEND_LOADING).assertIsDisplayed()
    }

    @Test
    fun homeScreen_appendError_showsMappedMessageAndKeepsLoadedItems() {
        val appendError = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = false),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.Error(DataErrorException(DataError.Remote.NO_INTERNET)),
        )
        composeTestRule.setContent {
            val items = flowOf(
                PagingData.from(sampleMovies.take(2), sourceLoadStates = appendError),
            ).collectAsLazyPagingItems()
            MaterialTheme {
                HomeScreen(state = HomeUiState(), movies = items, onAction = {}, onMovieClick = { _, _ -> })
            }
        }
        composeTestRule.onNodeWithText(noInternetMessage).assertIsDisplayed()
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
                HomeScreen(state = HomeUiState(), movies = items, onAction = {}, onMovieClick = { _, _ -> })
            }
        }
        composeTestRule.waitUntil(TIMEOUT_MILLIS) {
            composeTestRule.onAllNodesWithTag(TmdbTestTags.ERROR).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag(TmdbTestTags.RETRY).performClick()
        composeTestRule.waitUntil(TIMEOUT_MILLIS) {
            composeTestRule.onAllNodesWithTag(TmdbTestTags.ERROR).fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun homeScreen_refreshLoadingWithStaleItems_keepsGrid() {
        composeTestRule.setContent {
            val items = flowOf(
                PagingData.from(sampleMovies, sourceLoadStates = REFRESHING),
            ).collectAsLazyPagingItems()
            MaterialTheme {
                HomeScreen(state = HomeUiState(), movies = items, onAction = {}, onMovieClick = { _, _ -> })
            }
        }
        composeTestRule.onNodeWithTag(HomeTestTags.GRID).assertIsDisplayed()
        composeTestRule.onNodeWithText(sampleMovies.first().title).assertIsDisplayed()
        composeTestRule.onNodeWithTag(HomeTestTags.LOADING).assertDoesNotExist()
    }

    @Test
    fun homeScreen_refreshErrorWithItems_keepsGridWithoutFullScreenError() {
        composeTestRule.setContent {
            val items = flowOf(
                PagingData.from(sampleMovies, sourceLoadStates = REFRESH_ERROR),
            ).collectAsLazyPagingItems()
            MaterialTheme {
                HomeScreen(state = HomeUiState(), movies = items, onAction = {}, onMovieClick = { _, _ -> })
            }
        }
        composeTestRule.onNodeWithTag(HomeTestTags.GRID).assertIsDisplayed()
        composeTestRule.onNodeWithText(sampleMovies.first().title).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TmdbTestTags.ERROR).assertDoesNotExist()
        composeTestRule.onNodeWithTag(HomeTestTags.LOADING).assertDoesNotExist()
    }

    @Test
    fun homeScreen_firstLoadWithoutItems_showsLoadingIndicator() {
        composeTestRule.setContent {
            val items = flowOf(
                PagingData.from(emptyList<Movie>(), sourceLoadStates = REFRESHING),
            ).collectAsLazyPagingItems()
            MaterialTheme {
                HomeScreen(state = HomeUiState(), movies = items, onAction = {}, onMovieClick = { _, _ -> })
            }
        }
        composeTestRule.onNodeWithTag(HomeTestTags.LOADING).assertIsDisplayed()
        composeTestRule.onNodeWithTag(HomeTestTags.GRID).assertDoesNotExist()
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
        composeTestRule.onNodeWithTag(TmdbTestTags.RETRY).performClick()
        assertTrue(retried)
    }

    @Test
    fun refreshErrorWithItems_showsSnackbarWithRetryAction() {
        setSnackbarEffect(
            MutableStateFlow(PagingData.from(sampleMovies, sourceLoadStates = REFRESH_ERROR)),
        )
        awaitText(noInternetMessage)
        composeTestRule.onNodeWithText(noInternetMessage).assertIsDisplayed()
        composeTestRule.onNodeWithText(retryLabel).assertIsDisplayed()
    }

    @Test
    fun refreshErrorWithoutItems_doesNotShowSnackbar() {
        setSnackbarEffect(
            MutableStateFlow(PagingData.from(emptyList<Movie>(), sourceLoadStates = REFRESH_ERROR)),
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(noInternetMessage).assertDoesNotExist()
    }

    @Test
    fun loadingAfterRefreshError_keepsSnackbarOnScreen() {
        val pagingData =
            MutableStateFlow(PagingData.from(sampleMovies, sourceLoadStates = REFRESH_ERROR))
        setSnackbarEffect(pagingData)
        awaitText(noInternetMessage)
        pagingData.value = PagingData.from(sampleMovies, sourceLoadStates = REFRESHING)
        composeTestRule.onNodeWithText(noInternetMessage).assertIsDisplayed()
    }

    @Test
    fun snackbarRetryAction_reloadsTheFailedRefresh() {
        val pagingSourceFactory = RefreshControlledPagingSourceFactory(sampleMovies)
        val pagerFlow =
            Pager(
                config = PagingConfig(pageSize = 2, enablePlaceholders = false),
                pagingSourceFactory = pagingSourceFactory,
            ).flow
        lateinit var movies: LazyPagingItems<Movie>
        setSnackbarEffect(pagerFlow) { movies = it }
        composeTestRule.waitUntil(TIMEOUT_MILLIS) { movies.itemCount > 0 }
        pagingSourceFactory.failNextLoad = true
        composeTestRule.runOnIdle { movies.refresh() }
        awaitText(noInternetMessage)
        assertTrue(movies.itemCount > 0, "stale items should survive a failed refresh")
        composeTestRule.onNodeWithText(retryLabel).performClick()
        composeTestRule.waitUntil(TIMEOUT_MILLIS) { movies.loadState.refresh is LoadState.NotLoading }
        assertTrue(movies.itemCount > 0)
    }

    private fun setSnackbarEffect(
        pagingData: Flow<PagingData<Movie>>,
        onMovies: (LazyPagingItems<Movie>) -> Unit = {},
    ) {
        composeTestRule.setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val movies = pagingData.collectAsLazyPagingItems()
            onMovies(movies)
            MaterialTheme {
                Box {
                    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
                        RefreshErrorSnackbarEffect(movies)
                    }
                    SnackbarHost(snackbarHostState)
                }
            }
        }
    }

    private fun awaitText(text: String) = composeTestRule.waitUntil(TIMEOUT_MILLIS) {
        composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }

    private companion object {
        val SETTLED = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = true),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true),
        )

        val REFRESHING = SETTLED.copy(refresh = LoadState.Loading)

        val REFRESH_ERROR =
            SETTLED.copy(refresh = LoadState.Error(DataErrorException(DataError.Remote.NO_INTERNET)))

        const val TIMEOUT_MILLIS = 5_000L
    }
}

private class RefreshControlledPagingSourceFactory(private val movies: List<Movie>) :
    () -> PagingSource<Int, Movie> {
    var failNextLoad = false

    override fun invoke(): PagingSource<Int, Movie> = object : PagingSource<Int, Movie>() {
        override fun getRefreshKey(state: PagingState<Int, Movie>): Int? = null

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> = if (failNextLoad) {
            failNextLoad = false
            LoadResult.Error(DataErrorException(DataError.Remote.NO_INTERNET))
        } else {
            LoadResult.Page(movies, prevKey = null, nextKey = null)
        }
    }
}

private class AppendFailsOncePagingSource(private val firstPage: List<Movie>) : PagingSource<Int, Movie>() {
    private var appendAttempts = 0

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: 1
        if (page == 1) return LoadResult.Page(firstPage, prevKey = null, nextKey = 2)
        return if (appendAttempts++ == 0) {
            LoadResult.Error(DataErrorException(DataError.Remote.NO_INTERNET))
        } else {
            LoadResult.Page(emptyList(), prevKey = 1, nextKey = null)
        }
    }
}
